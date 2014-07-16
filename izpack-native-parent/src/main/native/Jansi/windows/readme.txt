Using the Windows msvcrt.dll
============================

See message thread for history:
   http://groups.google.com/group/jansi-dev/browse_thread/thread/e7867408d43660e1

Benefits
--------
No need for the VS redistributables.

crtdll.c
--------

A minimal entry point (_DllMainCRTStartup)
The "official" _DllMainCRTStartup is way more complicated (and has more dependencies),
but most of it is not needed if the thing is small, and plain C
(so no exceptions and other fancy C++ stuff)

msvcrt.lib
----------
The msvcrt.lib files (32/64 bit) are generated from the Win7 msvcrt.dll.
The msvcrt.def file is generated once "by hand" and submited as part of the project
The msvcrt.lib file is generated ad build time from msvcrt.def

   - dumpbin.exe /exports msvcrt.dll > msvcrt.dump
   - Created msvcrt.def from msvcrt.dump (just removed a bunch of extra info)
   - The msvcrt.def file in this project is an intersection of the 32/64 bit versions
     No problem, the excluded functions are really-really obscure.
     In fact, it is enough to list in msvcrt.def the few functions we use, and add more by hand as needed
   - Added as a custom build step lib /machine:X32 /def:msvcrt.def /out:msvcrt.lib
     (or /machine:X64, of course)

vs2010.vcxproj
--------------
     - Sources: added crtdll.c
     - Sources: added msvcrt.def, with a custom build step that generates the .lib
     - Linker - General: added $(IntDir) as "Additional Library Directory" (for msvcrt.lib)
     - Linker - Input: "Ignore All Default Libraries" (/NODEFAULTLIB)
     - Linker - Input: removed user32.lib; ... ;odbccp32.lib, and added msvcrt.lib;RunTmChk.lib instead
       (RunTmChk.lib is in the VS lib folder, and needed for security_check_cookie)
