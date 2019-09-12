#include <stdio.h>

int main(int argc, char *argv[]) {
  
  FILE *myfile;
  int i;
  unsigned char k[1];
  
  myfile = fopen(argv[1],"r");
  
  fread(k, 1, 1, myfile);
  printf("n=%d\n", k[0]);
  
  int result = test_mod(k[0]);
  printf("res=%d\n", result);
  
  
  return 0;
}

int test_mod(int x) {
  
  int mod = x % 3;
  
  if (mod == 0) {
    return 0;
  }
  if (mod == 1) {
    return 1;
  }
  if (mod == 2) {
    return 2;
  }
  return -1;
}

