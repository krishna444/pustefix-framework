package de.schlund.pfixcore.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.util.zip.ZipException;

import junit.framework.TestCase;

public class JarFileURLConnectionTest extends TestCase {
    
    private File testModuleFile;

    @Override
    protected void setUp() throws Exception {
        File dir = new File("src/test/resources");
        File[] files = dir.listFiles();
        for(File file:files) {
            if(file.isFile() && file.getName().contains("sample-module-A")) {
                testModuleFile = file;
                break;
            }
        }
    }
    
    @Override
    protected void tearDown() throws Exception {
        JarFileCache.getInstance().dispose();
    }

    public void testJarFileURL() throws Exception {
        String urlStr = "jar:"+testModuleFile.toURI().toString()+"!/";
        URL url = new URL(urlStr);   
        JarURLConnection con = (JarURLConnection)url.openConnection();
        JarURLConnection conFile = new JarFileURLConnection(url);
        assertEquals(con.getLastModified(), con.getLastModified());
        assertNotNull(con.getJarFile());
        assertNotNull(conFile.getJarFile());
    }
    
    public void testNonExistingJarFileURL() throws Exception {
        String urlStr = "jar:"+testModuleFile.toURI().toString()+"xxx!/";
        URL url = new URL(urlStr);   
        JarURLConnection con = (JarURLConnection)url.openConnection();
        JarURLConnection conFile = new JarFileURLConnection(url);
        assertEquals(con.getLastModified(), con.getLastModified());
        IOException error = null;
        try {
            con.getJarFile();
        } catch(ZipException x) {
            //before JDK 1.7
            error = x;
        } catch(FileNotFoundException x) {
            //since JDK 1.7
            error = x;
        } catch(NoSuchFileException x) {
            //since Java 9
            error = x;
        }
        assertNotNull(error);
        error = null;
        try {
            conFile.getJarFile();
        } catch(ZipException x) {
            //before JDK 1.7
            error = x;
        } catch(FileNotFoundException x) {
            //since JDK 1.7
            error = x;
        } catch(NoSuchFileException x) {
            //since Java 9
            error = x;
        }
        assertNotNull(error);
    }
    
    public void testJarEntryURL() throws Exception {
        String urlStr = "jar:"+testModuleFile.toURI().toString()+"!" + ModuleDescriptor.DEFAULT_RESOURCE_PATH + "/txt/common.xml";
        URL url = new URL(urlStr);   
        JarURLConnection con = (JarURLConnection)url.openConnection();
        JarURLConnection conFile = new JarFileURLConnection(url);
        assertEquals(con.getContentLength(), conFile.getContentLength());
        assertNotNull(con.getJarEntry());
        assertNotNull(conFile.getJarEntry());
    }
    
    public void testNonExistingJarEntryURL() throws Exception {
        String urlStr = "jar:"+testModuleFile.toURI().toString()+"!" + ModuleDescriptor.DEFAULT_RESOURCE_PATH + "/txt/commonXXX.xml";
        URL url = new URL(urlStr);   
        JarURLConnection con = (JarURLConnection)url.openConnection();
        JarURLConnection conFile = new JarFileURLConnection(url);
        assertEquals(con.getLastModified(), con.getLastModified());
        FileNotFoundException error = null;
        try {
            con.getJarEntry();
        } catch(FileNotFoundException x) {
            error = x;
        }
        assertNotNull(error);
        error = null;
        try {
            conFile.getJarEntry();
        } catch(FileNotFoundException x) {
            error = x;
        }
        assertNotNull(error);
    }

    public void testEmptyJarEntryURL() throws Exception {
        String urlStr = "jar:"+testModuleFile.toURI().toString()+"!/";
        URL url = new URL(urlStr);   
        JarURLConnection con = (JarURLConnection)url.openConnection();
        JarURLConnection conFile = new JarFileURLConnection(url);
        assertNull(con.getJarEntry());
        assertNull(conFile.getJarEntry());
    }
    
}
