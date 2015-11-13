/*
 * $Id: UnicodeHelper.h,v 1.2 2009/03/04 07:39:16 blf Exp $
 * IzPack - Copyright 2001-2005 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://izpack.codehaus.org/
 * 
 * Copyright 2005 Klaus Bartz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* Stuff for differ between UNICODE and MBCS 
 * May be this work only with visual studio 6.x
 * ----------------- START ---------------------
 */
 
#ifdef _UNICODE
#ifndef UNICODE
	#define UNICODE
#endif
#define NEW_STRING(a)		NewString((const jchar*)a, (jsize)_tcslen(a))
#define GET_STRING_CHARS	GetStringChars
#define RELEASE_STRING_CHARS(a,b) ReleaseStringChars(a, (const jchar*)b)
#else
#define NEW_STRING(a)		NewStringUTF(a)
#define GET_STRING_CHARS	GetStringUTFChars
#define RELEASE_STRING_CHARS(a,b) ReleaseStringUTFChars(a, (const jchar*)b)
#endif

#include <tchar.h>

/* Stuff for differ between UNICODE and MBCS 
 * May be this work only with visual studio 6.x
 * ----------------- END ---------------------
 */
