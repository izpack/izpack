package com.izforge.izpack.util.os;

import java.io.File;

import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.util.DefaultTargetPlatformFactory;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.Librarian;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;
import com.izforge.izpack.util.TargetFactory;

public class WinSetupAPIMain
{

    /**
     * Small test of functionality of the WIndows Setup API. Usage:<br>
     * <code>
     * c(opy) sourcefile destfile<br>
     * m(ove)/rename sourcefile destfile<br>
     * d(elete) file<br>
     * </code>
     *
     * @param args See above usage notes
     */
    public static void main(String[] args)
    {

        if (args.length > 0)
        {

            try
            {
                ObjectFactory dummy = new ObjectFactory()
                {
                    @Override
                    public <T> T create(Class<T> type, Object... parameters)
                    {
                        return null;
                    }

                    @Override
                    public <T> T create(String className, Class<T> superType, Object... parameters)
                    {
                        return null;
                    }
                };
                Platforms platforms = new Platforms();
                Platform platform = platforms.getCurrentPlatform();
                TargetFactory factory = new TargetFactory(new DefaultTargetPlatformFactory(dummy, platform, platforms));
                Librarian librarian = new Librarian(factory, new Housekeeper());
                System.out.println("(Java) Opening new file queue...");
                WinSetupFileQueue fq = new WinSetupFileQueue(librarian, new WinSetupDefaultCallbackHandler());

                String name = args[0];
                char c = name.charAt(0);
                switch (c)
                {
                    case 'c':
                        if (args.length == 3)
                        {
                            File srcfile = new File(args[1]);
                            File destfile = new File(args[2]);
                            System.out.println("(Java) Copying " + args[1] + " -> " + args[2]);
                            fq.addCopy(srcfile, destfile);
                        }
                        else
                        {
                            usageNotes();
                        }
                        break;
                    case 'm':
                        if (args.length == 3)
                        {
                            File srcfile = new File(args[1]);
                            File destfile = new File(args[2]);
                            System.out.println("(Java) Renaming/Moving " + args[1] + " -> " + args[2]);
                            // fq.addRename( srcfile, destfile );
                            fq.addMove(srcfile, destfile);
                        }
                        else
                        {
                            usageNotes();
                        }
                        break;
                    case 'd':
                        if (args.length == 2)
                        {
                            File file = new File(args[1]);
                            System.out.println("(Java) Deleting " + args[1]);
                            fq.addDelete(file);
                        }
                        else
                        {
                            usageNotes();
                        }
                        break;
                    default:
                        usageNotes();
                        break;
                }

                System.out.println("(Java) Committing file queue...");
                fq.commit();

                System.out.println("(Java) Closing file queue...");
                fq.close();
            }
            catch (Exception e)
            {
                System.err.println("Command failed due to " + e.getMessage());
            }
            finally
            {
                System.exit(1);
            }

        }
        else
        {
            usageNotes();
        }
    }

    private static void usageNotes()
    {
        System.out.println("Usage:");
        System.out.println("c(opy) <sourcefile> <destfile>");
        System.out.println("m(ove)/rename <sourcefile> <destfile>");
        System.out.println("d(elete) <file>");
    }

}
