#include <stdio.h>

// BEGIN YN: handling of hash maps in C

#include "uthash.h"
#include "utarray.h"

/* Represents an entry in the hash map <int, set<int>>. */
struct hash_map_entry {
  
  /* key */
  int id;
  
  /* value */
  UT_array *value_set;
  
  /* internal hash handler */
  UT_hash_handle hh; /* makes this structure hashable */
  
};

/* Compare function for integers. */
int intsort(const void *a, const void *b) {
  int _a = *(const int *)a;
  int _b = *(const int *)b;
  return (_a < _b) ? -1 : (_a > _b);
}

/* Searches for entry in hash map with given id. */
struct hash_map_entry *find_entry_in_hashmap(int id, struct hash_map_entry **hash_map) {
  struct hash_map_entry *entry;
  HASH_FIND_INT(*hash_map, &id, entry);
  return entry;
}

/* Checks if key-value-combination is new for this hash map. */
int exists_not_in_hashmap(int id, int value, struct hash_map_entry **hash_map) {
  struct hash_map_entry *entry;
  entry = find_entry_in_hashmap(id, hash_map);
  if (entry == NULL) {
    return 1;
  } else {
    if (utarray_find(entry->value_set, &value, intsort) == NULL) {
      return 1;
    } else {
      return 0;
    }
  }
}

/* Add key-value-combination if new. Returns 1 if it was sucessfull added, 0 for not. */
int add_if_new(int id, int value, struct hash_map_entry **hash_map) {
  struct hash_map_entry *entry;
  UT_array *value_set;
  
  /* Check if id is already existent. */
  entry = find_entry_in_hashmap(id, hash_map);
  if (entry == NULL) {

    /* If not create it. */
    entry = (struct hash_map_entry *) malloc(sizeof *entry);
    
    /* Add new key. */
    entry->id = id;
    
    /* Add set consisting only of given value */
    utarray_new(value_set, &ut_int_icd);
    utarray_push_back(value_set, &value);
    entry->value_set = value_set;
    
    /* Add everything to the map structure. */
    HASH_ADD_INT(*hash_map, id, entry);
    
    return 1;
    
  } else {
    
    /* Check if v2_encoding already exists */
    if (utarray_find(entry->value_set, &value, intsort) == NULL) {
      
      /* If it does not exist, then add it and resort the array. */
      utarray_push_back(entry->value_set, &value);
      utarray_sort(entry->value_set, intsort);
      
      return 1;
    } else {
      
      /* Already existent, so don't change something. */
      return 0;
    }
    
  }
  
}

void print_map(struct hash_map_entry **hash_map) {
  struct hash_map_entry *entry;
  for(entry=*hash_map; entry != NULL; entry=(struct hash_map_entry*)(entry->hh.next)) {
    printf("id: %d -> (", entry->id);
    int *p = NULL;
    while((p = (int*) utarray_next(entry->value_set, p))) {
      printf("%d,", *p);
    }
    printf(")\n");
  }
  printf("\n");
}

void delete_all(struct hash_map_entry **hash_map) {
  struct hash_map_entry *current_entry, *tmp;
  
  HASH_ITER(hh, *hash_map, current_entry, tmp) {
    
    /* Delete set in this node. */
    utarray_free(current_entry->value_set);
    
    /* delete it (users advances to next) */
    HASH_DEL(*hash_map, current_entry);
    
    /* free it */
    free(current_entry);
    
  }
}

// END YN

// Just for testing
/*int main(int argc, char** argv) {
  
  
  int v1_out_1 = 2;
  int v2_out_1 = 3;
  int v2_out_2 = 3;
  int v2_out_3 = 1;
  
  int res = add_if_new(v1_out_1, v2_out_1, &output_diff_map);
  printf("Try to add (%d->%d): %d\n", v1_out_1, v2_out_1, res);
  print_map(&output_diff_map);
  
  res = add_if_new(v1_out_1, v2_out_1, &output_diff_map);
  printf("Try to add (%d->%d): %d\n", v1_out_1, v2_out_1, res);
  print_map(&output_diff_map);
  
  res = add_if_new(v1_out_1, v2_out_2, &output_diff_map);
  printf("Try to add (%d->%d): %d\n", v1_out_1, v2_out_1, res);
  print_map(&output_diff_map);
  
  res = add_if_new(v1_out_1, v2_out_3, &output_diff_map);
  printf("Try to add (%d->%d): %d\n", v1_out_1, v2_out_3, res);
  print_map(&output_diff_map);
  
  res = add_if_new(2, 3, &output_diff_map);
  printf("Try to add (%d->%d): %d\n", 2, 3, res);
  print_map(&output_diff_map);
  
  res = add_if_new(3, 1, &output_diff_map);
  printf("Try to add (%d->%d): %d\n", 3, 1, res);
  print_map(&output_diff_map);
  
  res = add_if_new(3, 1, &output_diff_map);
  printf("Try to add (%d->%d): %d\n", 3, 1, res);
  print_map(&output_diff_map);
  
 
  printf("Cleaning..\n");
  delete_all(&output_diff_map);
  printf("Done.\n");
}*/
