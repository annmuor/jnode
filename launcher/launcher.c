/*
 * Licensed to the jNode FTN Platform Develpoment Team (jNode Team)
 * under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for 
 * additional information regarding copyright ownership.  
 * The jNode Team licenses this file to you under the 
 * Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

#ifdef UNIX
#define FILE_SEPARATOR "/"
#define CLASSPATH_SEPARATOR ":"
#define JNODE_DEF_LOCATION "/opt/jnode/"
#define JAVA_BINARY "java"
#endif

#ifdef WINDOWS
#define FILE_SEPARATOR "\\"
#define CLASSPATH_SEPARATOR ";"
#define JNODE_DEF_LOCATION "C:\\Program Files\\jnode\\";
#define JAVA_BINARY "java.exe"
#endif

#ifndef JNODE_CLASS
#define JNODE_CLASS "jnode.main.Main"
#endif
#ifndef JNODE_LOCATION
#define JNODE_LOCATION JNODE_DEF_LOCATION
#endif

#ifndef JNODE_CONF
#define JNODE_CONF JNODE_LOCATION FILE_SEPARATOR "etc" FILE_SEPARATOR "jnode.conf"
#endif

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <dirent.h>

#ifndef TRUE
#define TRUE 1
#endif

#ifndef FALSE
#define FALSE 0
#endif



static int flag_display = FALSE;
static char *java_path = FALSE;

static int check_version(FILE *fp) {
    char c;
    while(TRUE) {
	fread(&c, 1,1,fp);
	if(c == '1') {
	    fread(&c,1,1,fp);
	    if(c == '.') {
		fread(&c,1,1,fp);
		if(c == '7') {
		    fread(&c,1,1,fp);
		    if(c == '.') {
			return TRUE;
		    }
		}
	    }
	}
	if(feof(fp)) {
	    break;
	}
    }
    return FALSE;
}

static int check_java() {
    int file_exists = FALSE;
    int java_version = FALSE;
    if(java_path) {
	// 1. check exists
	FILE *fp = fopen(java_path, "r");
	if(fp) {
	    file_exists = TRUE;
	    java_version = check_version(fp);
	    fclose(fp);
	}
    }
    return (file_exists && java_version);
}

static int filter(const struct dirent *dir) {
    if(dir) {
	char *last = strrchr(dir->d_name, '.');
	if(last) {
	    if(!strcmp(last, ".jar")) {
		return TRUE;
	    }
	}
    }
    return FALSE;
}

int main(int argc, const char **argv, const char **env) {
    #ifdef UNIX
    while(*env) {
	if(!strncmp("DISPLAY=",*env,8)) {
	    flag_display = TRUE;
	} else if(!strncmp("PATH=",*env,5)) {
	    // check java binary
	    char *path = (char*)&((*env)[5]);
	    char *path_fragment;
	    while((path_fragment = strrchr(path, ':'))) {
		int java_path_len = strlen(path_fragment)+strlen(FILE_SEPARATOR)+strlen(JAVA_BINARY);
		java_path = (char *)malloc(java_path_len);
		memset(java_path, 0, java_path_len);
		strcat(java_path, &path_fragment[1]);
		strcat(java_path, FILE_SEPARATOR);
		strcat(java_path, JAVA_BINARY);
		int test = check_java();
		if(test == TRUE) {
		    break;
		} else {
		    free(java_path);
		    java_path = FALSE;
		    *path_fragment = FALSE;
		}
	    }
	}
	env++;
    }
    #else
	// TODO windows java check ?
	flag_display = TRUE;
	java_path = JAVA_BINARY;
    #endif
    if(java_path) {
	char *lib = JNODE_LOCATION FILE_SEPARATOR "lib";
	int lib_len = strlen(lib) + strlen(FILE_SEPARATOR);
	char *classpath = FALSE;
	int n = 0;
	struct dirent **jars;
	n = scandir(lib, &jars, filter, NULL);
	if(n > 0) {
	    int clen = 0;
	    while(n--) {
		if(!classpath) {
		     clen += lib_len + strlen(jars[n]->d_name);
		     classpath = (char *)malloc(clen+1);
		     memset(classpath, 0, clen);
		} else {
		    clen += strlen(CLASSPATH_SEPARATOR) + lib_len + strlen(jars[n]->d_name);
		    classpath = (char *)realloc(classpath, clen+1);
		    strcat(classpath, CLASSPATH_SEPARATOR);
		}
		strcat(classpath, lib);
		strcat(classpath, FILE_SEPARATOR);
		strcat(classpath, jars[n]->d_name);
	    }
	    int exec_len = 10+strlen(java_path)+clen+strlen(JNODE_CLASS)+strlen(JNODE_CONF);
	    char *exec = (char *)malloc(exec_len);
	    snprintf(exec, exec_len, "%s -cp \"%s\" %s %s", java_path, classpath, JNODE_CLASS, JNODE_CONF);
	    system(exec);
	} else {
	    printf("No jars found in %s\n", lib);
	}
    } else {
	printf("Java 1.7 not found on your system\n");
    }
    return 0;
}
