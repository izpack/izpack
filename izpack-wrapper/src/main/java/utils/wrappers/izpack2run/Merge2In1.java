/*
 * Copyright 2019 gucchino.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package utils.wrappers.izpack2run;

/**
 *
 * @author gucchino
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.lang.System.exit;

public class Merge2In1 {

    public static void Merge2In1(File file1, File file2, File outputFile) throws IOException {

        FileInputStream fis1 = new FileInputStream(file1);
        FileInputStream fis2 = new FileInputStream(file2);
        FileOutputStream fos = new FileOutputStream(outputFile);

        int len = 0;

        byte[] buf = new byte[1024 * 1024]; // 1MB buffer

        while ((len = fis1.read(buf)) != -1) {
            fos.write(buf, 0, len);
        }

        while ((len = fis2.read(buf)) != -1) {
            fos.write(buf, 0, len);
        }

        fos.close();
    }
    
    public static void main(String[] argv) throws IOException {
        
        if(argv.length != 3) {
            System.out.println("3 parameters required: file-input1, file-input2, file-output3");
            exit(0);
        }
        
        File f1 = new File(argv[0]);
        File f2 = new File(argv[1]);
        File f3 = new File(argv[2]);
        
        Merge2In1(f1,f2,f3);
    }    
}
