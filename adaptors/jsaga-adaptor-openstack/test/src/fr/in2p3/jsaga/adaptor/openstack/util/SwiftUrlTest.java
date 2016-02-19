package fr.in2p3.jsaga.adaptor.openstack.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class SwiftUrlTest {

    private static String m_swiftPath = "v2.0/object-store/containers/myContainer/dir/subdir";
//    public static String m_swiftURL = "openstack://host:5000/" + m_swiftPath;
    
    @Test
    public void nova() {
        assertEquals("v2.0/", SwiftURL.getNovaPath(m_swiftPath));
        assertEquals("myContainer/dir/subdir", SwiftURL.getContainerAndPath(m_swiftPath));
        assertEquals("myContainer", SwiftURL.getContainer(m_swiftPath));
        assertEquals("dir/subdir", SwiftURL.getPath(m_swiftPath));
        assertEquals("file", SwiftURL.getFileName("dir/subdir/file"));
        assertEquals("subdir", SwiftURL.getFileName("dir/subdir/"));
        assertEquals("dir", SwiftURL.getFileName("dir/"));
        assertEquals("file", SwiftURL.getFileName("file"));
        assertEquals("dir/subdir", SwiftURL.getDirectoryName("dir/subdir/file"));
        assertEquals("dir", SwiftURL.getDirectoryName("dir/subdir/"));
        assertNull(SwiftURL.getDirectoryName("dir/"));
        assertNull(SwiftURL.getDirectoryName("file"));
    }
}
