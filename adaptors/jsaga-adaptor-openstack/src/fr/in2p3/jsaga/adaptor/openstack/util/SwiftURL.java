package fr.in2p3.jsaga.adaptor.openstack.util;

import org.openstack4j.api.types.ServiceType;
import org.openstack4j.model.storage.object.options.ObjectLocation;


public final class SwiftURL {
    
    public static ObjectLocation getObjectLocation(String swiftPath) {
        return ObjectLocation.create(
                SwiftURL.getContainer(swiftPath),
                SwiftURL.getPath(swiftPath)
        );
    }
    /*
     * "v2.0/object-store/containers/myContainer" => "v2.0/"
     */
    public static String getNovaPath(String swiftUrl) {
        return swiftUrl.replaceAll(ServiceType.OBJECT_STORAGE.getServiceName() + ".*$", "");
    }
    
    /*
     * "v2.0/object-store/containers/myContainer/myFile" => "myContainer/myFile"
     */
    public static String getContainerAndPath(String swiftPath) {
        return swiftPath.replaceAll("^.*" + ServiceType.OBJECT_STORAGE.getServiceName() + "/containers/", "");
    }
    
    /*
     * "v2.0/object-store/containers/myContainer/myFile" => "myContainer"
     */
    public static String getContainer(String swiftPath) {
        String containerAndPath = getContainerAndPath(swiftPath);
        return containerAndPath.replaceAll("/.*$", "");
    }
    
    /*
     * "v2.0/object-store/containers/myContainer/myFile" => "myFile"
     */
    public static String getPath(String swiftPath) {
        String containerAndPath = getContainerAndPath(swiftPath);
        return containerAndPath.substring(containerAndPath.indexOf("/")+1);
    }
    
    /*
     * "myDir/mySubDir/" => "mySubDir"
     * "myDir/mySubDir/myFile" => "myFile"
     * "file" => "file"
     */
    public static String getFileName(String swiftPath) {
        String filename = swiftPath;
        if (!filename.contains("/")) {
            return filename;
        }
        if (swiftPath.endsWith("/")) {
            filename = filename.substring(0, filename.length()-1);
        }
        return filename.replaceAll(".*/", "");
    }
    
    /*
     * "myDir/mySubDir/" => "myDir"
     * "myDir/mySubDir/myFile" => "myDir/mySubDir"
     * "file" => null
     */
    public static String getDirectoryName(String swiftPath) {
        String filename = swiftPath;
        // file with no directory
        if (!filename.contains("/")) {
            return null;
        }
        if (swiftPath.endsWith("/")) {
            filename = filename.substring(0, filename.length()-1);
            // directory with no subdir
            if (!filename.contains("/")) {
                return null;
            }
        }
        return filename.substring(0, filename.lastIndexOf("/"));
    }
}
