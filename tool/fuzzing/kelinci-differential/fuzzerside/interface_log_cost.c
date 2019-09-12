#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <errno.h>
#include <string.h>
#include <unistd.h>
#include <netdb.h>
#include <sys/socket.h>
#include <netinet/in.h>

#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/shm.h>
#include <sys/wait.h>

#define FILE_READ_CHUNK 1024
#define SHM_SIZE 65536
#define SOCKET_READ_CHUNK 1024 // SHM_SIZE should be divisable by this

#define SHM_ENV_VAR "__AFL_SHM_ID"

#define LOGFILE "/tmp/afl-wrapper.log"

#define VERBOSE

#define STATUS_SUCCESS 0
#define STATUS_TIMEOUT 1
#define STATUS_CRASH 2
#define STATUS_QUEUE_FULL 3
#define STATUS_COMM_ERROR 4
#define STATUS_DONE 5

#define MAX_TRIES 40

#define DEFAULT_SERVER "localhost"
#define DEFAULT_PORT "7007"
#define DEFAULT_NUMBER_OF_TARGETS 0

/* YN: +24 for resource results +8 for user defined cost +19 for regression metrics (patch distances not included) = +51 */
static int TOTAL_TRACE_BITS_SIZE = SHM_SIZE+51;

#define DEFAULT_MODE 0
#define LOCAL_MODE 1

uint8_t* trace_bits;
int prev_location = 0;

/* Stdout is piped to null when running inside AFL, so we have an option to write output to a file */
FILE* logfile;

/* Running inside AFL or standalone */
uint8_t in_afl = 0;

#define OUTPUT_STDOUT
#define OUTPUT_FILE

void LOG(const char* format, ...) {
  va_list args;
#ifdef OUTPUT_STDOUT
    va_start(args, format);
    vprintf(format, args);
#endif
#ifdef OUTPUT_FILE
    char message[150], *ptr = message;
    ptr += sprintf(message, "[%d] ", getpid());

    va_start(args, format);
    vsprintf(ptr, format, args);
    fprintf(logfile, "%s", message);
//    vfprintf(logfile, format, args);
#endif
  va_end(args);
}

#define DIE(...) { LOG(__VA_ARGS__); if(!in_afl) shmdt(trace_bits); if(logfile != NULL) fclose(logfile); exit(1); }
#define LOG_AND_CLOSE(...) { LOG(__VA_ARGS__); if(logfile != NULL) fclose(logfile); }

#ifdef VERBOSE
  #define LOGIFVERBOSE(...) LOG(__VA_ARGS__);
#else
  #define LOGIFVERBOSE(...)
#endif

// YN: costs
#define SEPARATOR ';' // file names contain commas...
FILE* costs_file;

// Init file
static void init_costs_file(const char* costfName) {
  char path[strlen(costfName)+2];
  snprintf(path, sizeof path, "%s", costfName);
  costs_file = fopen(path, "a");
  if (!costs_file)
  DIE("Failed to open costs file: %s\n", path);
  //fprintf(costs_file, "Input file%c Time%c Memory%c Instr.%c User-Defined%c Patch-Touch%c Patch-Dist.%c Output Diff.%c Decision Dist.\n", SEPARATOR, SEPARATOR, SEPARATOR, SEPARATOR, SEPARATOR, SEPARATOR, SEPARATOR,  SEPARATOR);
}

// Append resource usage to output file.
static void append_resource_use_to_file(const char* fname, long time, long memory, long instr_cost, long user_defined_cost, char patch_touched, int patch_distances[], int patch_distances_len, char output_diff, int dec_hist_distance) {
  
  /* YN: transform int array to string */
  char* converted_patch_distances = "";
  char* distance = malloc(17);
  for (int i=0; i<patch_distances_len; i++) {
    if (i < patch_distances_len-1) {
      snprintf(distance, 17, "%d,", patch_distances[i]);
    } else {
      snprintf(distance, 17, "%d", patch_distances[i]);
    }
    char* tmp = (char *) malloc(1 + strlen(converted_patch_distances) + strlen(distance));
    strcpy(tmp, converted_patch_distances);
    strcat(tmp, distance);
    converted_patch_distances = (char *) malloc(1 + strlen(tmp));
    strcpy(converted_patch_distances, tmp);
    free(tmp);
  }
  free(distance);
  
  fprintf(costs_file, "%s%c %ld%c %ld%c %ld%c %ld%c %u%c [%s]%c %u%c %d\n", fname, SEPARATOR, time, SEPARATOR, memory, SEPARATOR, instr_cost, SEPARATOR, user_defined_cost, SEPARATOR, patch_touched, SEPARATOR, converted_patch_distances, SEPARATOR, output_diff, SEPARATOR, dec_hist_distance);
  if (patch_distances_len > 0) {
    free(converted_patch_distances);
  }
}

int tcp_socket;

/* Set up the TCP connection */
void setup_tcp_connection(const char* hostname, const char* port) {
  LOG("Trying to connect to server %s at port %s...\n", hostname, port);
  struct addrinfo hints;
  memset(&hints, 0, sizeof(hints));
  hints.ai_family = AF_UNSPEC;
  hints.ai_socktype = SOCK_STREAM;
  hints.ai_protocol = 0;
  hints.ai_flags = AI_ADDRCONFIG;
  struct addrinfo* res = 0;
  int err = getaddrinfo(hostname, port, &hints, &res);
  if (err!=0) {
    DIE("failed to resolve remote socket address (err=%d)\n", err);
  }

  tcp_socket = socket(res->ai_family, res->ai_socktype, res->ai_protocol);
  if (tcp_socket == -1) {
    DIE("%s\n", strerror(errno));
  }

  if (connect(tcp_socket, res->ai_addr, res->ai_addrlen) == -1) {
    DIE("%s\n", strerror(errno));
  }

  freeaddrinfo(res);
}

void printUsageAndDie() {
  DIE("Usage: interface [-s <server>] [-p <port>] [-t <number_of_targets>] <filename> <log_file_name>\n");
}

int main(int argc, char** argv) {

  /* Stdout is piped to null, so write output to a file */
#ifdef OUTPUT_FILE
  logfile = fopen(LOGFILE, "wb");
  if (logfile == NULL) {
    DIE("Error opening log file for writing\n");
  }
#endif

  /* Parameters */
  const char* filename;
  char* server = DEFAULT_SERVER;
  char* port = DEFAULT_PORT;
  int number_of_targets = DEFAULT_NUMBER_OF_TARGETS;

  /* Check num of parameters */
  if (argc < 3) {
    LOG("1");
    printUsageAndDie();
  }

  /* Parse parameters */
  int curArg = 1;
  while (curArg < argc) {
    if (argv[curArg][0] == '-') { //flag
      if (argv[curArg][1] == 's') {
        // set server
  server = argv[curArg+1];
  curArg += 2;
      } else if (argv[curArg][1] == 'p') {
        // set port
  port = argv[curArg+1];
  curArg += 2;
      } else if (argv[curArg][1] == 't') { // YN: targets
        sscanf (argv[curArg+1], "%d", &number_of_targets);
        TOTAL_TRACE_BITS_SIZE = TOTAL_TRACE_BITS_SIZE + number_of_targets * 4;
        curArg += 2;
      } else {
        LOG("Unkown flag: %s\n", argv[curArg]);
  printUsageAndDie();
      }
    } else {
      break; // expect filename now
    }
  }
  if (curArg != argc-2) {
    LOG("3");
    printUsageAndDie();
  }
  filename = argv[curArg];
  LOG("input file = %s\n", filename);
  
  const char* cost_file_name;
  cost_file_name = argv[curArg+1];
  init_costs_file(cost_file_name);

  /* Local mode? */
  uint8_t mode = DEFAULT_MODE;
  if (strcmp(server, "localhost") == 0) {
    LOG("Running in LOCAL MODE.\n");
    mode = LOCAL_MODE;
  }

  /* Preamble instrumentation */
  char* shmname = getenv(SHM_ENV_VAR);
  int status = 0;
  uint8_t kelinci_status = STATUS_SUCCESS;
  if (shmname) {

    /* Running in AFL */
    in_afl = 1;

    /* Set up shared memory region */
    LOG("SHM_ID: %s\n", shmname);
    key_t key = atoi(shmname);

    if ((trace_bits = shmat(key, 0, 0)) == (uint8_t*) -1) {
      DIE("Failed to access shared memory 2\n");
    }
    LOGIFVERBOSE("Pointer: %p\n", trace_bits);
    LOG("Shared memory attached. Value at loc 3 = %d\n", trace_bits[3]);

    /* Set up the fork server */
    LOG("Starting fork server...\n");
    if (write(199, &status, 4) != 4) {
      LOG("Write failed\n");
      goto resume;
    }

    while(1) {
      if(read(198, &status, 4) != 4) {
         DIE("Read failed\n");
      }

      int child_pid = fork();
      if (child_pid < 0) {
        DIE("Fork failed\n");
      } else if (child_pid == 0) {
        LOGIFVERBOSE("Child process, continue after pork server loop\n");
  break;
      }

      LOGIFVERBOSE("Child PID: %d\n", child_pid);
      write(199, &child_pid, 4);

      LOGIFVERBOSE("Status %d \n", status);

      if(waitpid(child_pid, &status, 0) <= 0) {
        DIE("Fork crash");
      }

      LOGIFVERBOSE("Status %d \n", status);
      write(199, &status, 4);
    }

    resume:
    LOGIFVERBOSE("AFTER LOOP\n\n");
    close(198);
    close(199);

    /* Mark a location to show we are instrumented */
    trace_bits[0]++;

  } else {
    LOG("Not running within AFL. Shared memory and fork server not set up.\n");
    trace_bits = (uint8_t*) malloc(TOTAL_TRACE_BITS_SIZE);
  }

  /* Done with initialization, now let's start the wrapper! */
  int try = 0;
  size_t nread;
  char buf[FILE_READ_CHUNK];
  FILE *file;
  uint8_t conf = STATUS_DONE;

  // try up to MAX_TRIES time to communicate with the server
  do {
    // if this is not the first try, sleep for 0.1 seconds first
    if(try > 0)
      usleep(100000);

    setup_tcp_connection(server, port);

    /* Send mode */
    write(tcp_socket, &mode, 1);

    /* LOCAL MODE */
    if (mode == LOCAL_MODE) {

      // get absolute path
      char path[10000];
      realpath(filename, path);

      // send path length
      int pathlen = strlen(path);
      if (write(tcp_socket, &pathlen, 4) != 4) {
        DIE("Error sending path length");
      }
      LOG("Sent path length: %d\n", pathlen);

      // send path
      if (write(tcp_socket, path, pathlen) != pathlen) {
        DIE("Error sending path");
      }
      LOG("Sent path: %s\n", path);


    /* DEFAULT MODE */
    } else {

      /* Send file contents */
      file = fopen(filename, "r");
      if (file) {

        // get file size and send
        fseek(file, 0L, SEEK_END);
        int filesize = ftell(file);
        rewind(file);
        LOG("Sending file size %d\n", filesize);
        if (write(tcp_socket, &filesize, 4) != 4) {
          DIE("Error sending filesize");
        }

        // send file bytes
        size_t total_sent = 0;
        while ((nread = fread(buf, 1, sizeof buf, file)) > 0) {
          if (ferror(file)) {
            DIE("Error reading from file\n");
          }
          ssize_t sent = write(tcp_socket, buf, nread);
          total_sent += sent;
          LOG("Sent %lu bytes of %lu\n", total_sent, filesize);
        }
        fclose(file);
      } else {
        DIE("Error reading file %s\n", filename);
      }
    }
    
    /* YN: Define array for patch distances. */
    int patch_distances[number_of_targets];

    /* Read kelinci_status over TCP */
    nread = read(tcp_socket, &kelinci_status, 1);
    if (nread != 1) {
      LOG("Failure reading exit status over socket.\n");
      kelinci_status = STATUS_COMM_ERROR;
      goto cont;
    }
    LOG("Return kelinci_status = %d\n", status);

    /* Read "shared memory" over TCP */
    uint8_t *shared_mem = malloc(SHM_SIZE);
//    for (int offset = 0; offset < SHM_SIZE; offset += SOCKET_READ_CHUNK) {
//      nread = read(tcp_socket, shared_mem+offset, SOCKET_READ_CHUNK);
//      if (nread != SOCKET_READ_CHUNK) {
//        LOG("Error reading from socket\n");
//        kelinci_status = STATUS_COMM_ERROR;
//        goto cont;
//      }
//    }
    int offset = 0;
    while (offset < SHM_SIZE) {
      nread = read(tcp_socket, shared_mem+offset, SHM_SIZE - offset);
      if (nread <= 0) {
        LOG("Error reading from socket. bytes read: %d\n", nread);
        kelinci_status = STATUS_COMM_ERROR;
        goto cont;
      }
      offset += nread;
      LOG("Block read: %d bytes read\n", nread);
    }

    /* If successful, copy over to actual shared memory */
    for (int i = 0; i < SHM_SIZE; i++) {
      if (shared_mem[i] != 0) {
        LOG("%d -> %d\n", i, shared_mem[i]);
        trace_bits[i] += shared_mem[i];
      }
    }

    /* Receive runtime */
    long runtime;
    nread = read(tcp_socket, &runtime, 8);
    if (nread != 8) {
      LOG("Failed to read runtime\n");
      status = STATUS_COMM_ERROR;
      goto cont;
    }
    LOG("Runtime: %ld\n", runtime);

    /* Receive max heap */
    long max_heap;
    nread = read(tcp_socket, &max_heap, 8);
    if (nread != 8) {
      LOG("Failed to read max_heap\n");
      status = STATUS_COMM_ERROR;
      goto cont;
    }
    LOG("Max heap: %ld\n", max_heap);

    /* Receive cost measured via instrumentation. */
    long instr_cost;
    nread = read(tcp_socket, &instr_cost, 8);
    if (nread != 8) {
      LOG("Failed to read instr_cost\n");
      status = STATUS_COMM_ERROR;
      goto cont;
    }
    LOG("Instr. cost: %ld\n", instr_cost);

    /* YN: Receive user-defined cost */
    long user_defined_cost;
    nread = read(tcp_socket, &user_defined_cost, 8);
    if (nread != 8) {
      LOG("Failed to read user_defined_cost\n");
      status = STATUS_COMM_ERROR;
      goto cont;
    }
    LOG("User-defined cost: %ld\n", user_defined_cost);
    
    /* YN: receive regression metrics */
    signed char output_diff;
    nread = read(tcp_socket, &output_diff, 1);
    if (nread != 1) {
      LOG("Failed to read output_diff\n");
      status = STATUS_COMM_ERROR;
      goto cont;
    }
    LOG("Output Diff: %u\n", output_diff);
    
    signed char new_crash;
    nread = read(tcp_socket, &new_crash, 1);
    if (nread != 1) {
      LOG("Failed to read output_diff\n");
      status = STATUS_COMM_ERROR;
      goto cont;
    }
    LOG("New crash: %u\n", new_crash);
    
    int output_v1_encoding;
    nread = read(tcp_socket, &output_v1_encoding, 4);
    if (nread != 4) {
      LOG("Failed to read output_v1_encoding\n");
      status = STATUS_COMM_ERROR;
      goto cont;
    }
    LOG("Encoded output v1: %d\n", output_v1_encoding);
    
    int output_v2_encoding;
    nread = read(tcp_socket, &output_v2_encoding, 4);
    if (nread != 4) {
      LOG("Failed to read output_v2_encoding\n");
      status = STATUS_COMM_ERROR;
      goto cont;
    }
    LOG("Encoded output v2: %d\n", output_v2_encoding);
    
    int dec_hist_distance;
    nread = read(tcp_socket, &dec_hist_distance, 4);
    if (nread != 4) {
      LOG("Failed to read dec_hist_distance\n");
      status = STATUS_COMM_ERROR;
      goto cont;
    }
    LOG("Decision Hist. Dist.: %d\n", dec_hist_distance);
    
    int dec_hist_diff_encoding;
    nread = read(tcp_socket, &dec_hist_diff_encoding, 4);
    if (nread != 4) {
      LOG("Failed to read dec_hist_diff_encoding\n");
      status = STATUS_COMM_ERROR;
      goto cont;
    }
    LOG("Encoded Decision Hist. Diff.: %d\n", dec_hist_diff_encoding);
    
    char patch_touched;
    nread = read(tcp_socket, &patch_touched, 1);
    if (nread != 1) {
      LOG("Failed to read patch_touched\n");
      status = STATUS_COMM_ERROR;
      goto cont;
    }
    LOG("Touched Patch: %u\n", patch_touched);
    
    for (int i=0; i<number_of_targets; i++) {
      int patch_distance;
      nread = read(tcp_socket, &patch_distance, 4);
      if (nread != 4) {
        LOG("Failed to read patch_distance\n");
        status = STATUS_COMM_ERROR;
        goto cont;
      }
      patch_distances[i] = patch_distance;
      LOG("Patch distance: %d\n", patch_distances[i]);
    }
    
    // YN: log cost
    append_resource_use_to_file(filename, runtime, max_heap, instr_cost, user_defined_cost, patch_touched, patch_distances, number_of_targets, output_diff, dec_hist_distance);
    
    /* Copy resource results to shared memory */
    memcpy(&trace_bits[SHM_SIZE], &runtime, 8);
    memcpy(&trace_bits[SHM_SIZE+8], &max_heap, 8);
    memcpy(&trace_bits[SHM_SIZE+16], &instr_cost, 8);
    /* YN: copy user-defined cost as well to shared memory */
    memcpy(&trace_bits[SHM_SIZE+24], &user_defined_cost, 8);
    /* YN: regression metrics */
    memcpy(&trace_bits[SHM_SIZE+32], &output_diff, 1);
    memcpy(&trace_bits[SHM_SIZE+33], &new_crash, 1);
    memcpy(&trace_bits[SHM_SIZE+34], &output_v1_encoding, 4);
    memcpy(&trace_bits[SHM_SIZE+38], &output_v2_encoding, 4);
    memcpy(&trace_bits[SHM_SIZE+42], &dec_hist_distance, 4);
    memcpy(&trace_bits[SHM_SIZE+46], &dec_hist_diff_encoding, 4);
    memcpy(&trace_bits[SHM_SIZE+50], &patch_touched, 1);
    for (int i=0; i<number_of_targets; i++) {
      memcpy(&trace_bits[SHM_SIZE+59+(i*4)], &patch_distances[i], 4);
    }
    
    /* Close socket */
cont: close(tcp_socket);

    /* Only try communicating MAX_TRIES times */
    if (try++ > MAX_TRIES) {
      // fail silently...
      DIE("Stopped trying to communicate with server.\n");
    }

  } while (kelinci_status == STATUS_QUEUE_FULL || kelinci_status == STATUS_COMM_ERROR);

  LOG("Received results. Terminating.\n\n");

  /* Disconnect shared memory */
  if (in_afl) {
    shmdt(trace_bits);
  }

  /* Terminate with CRASH signal if Java program terminated abnormally */
  if (kelinci_status == STATUS_CRASH) {
    LOG("Crashing...\n");
    abort();
  }

  /**
   * If JAVA side timed out, keep looping here till AFL hits its time-out.
   * In a good set-up, the time-out on the JAVA process is slightly longer
   * than AFLs time-out to prevent hitting this.
   **/
  if (kelinci_status == STATUS_TIMEOUT) {
    LOG("Starting infinite loop...\n");
    while (1) {
      sleep(10);
    }
  }

  LOG_AND_CLOSE("Terminating normally.\n");

  return 0;
}
