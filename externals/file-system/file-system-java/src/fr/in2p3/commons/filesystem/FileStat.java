package fr.in2p3.commons.filesystem;

import java.text.SimpleDateFormat;
import java.util.Date;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   FileStat
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   17 mai 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class FileStat {
    private static final int READ = 4;
    private static final int WRITE = 2;
    private static final int EXEC = 1;
    private static final SimpleDateFormat m_formatter = new SimpleDateFormat("MMM dd HH:mm");

    public String name;

    public boolean isdir;
    public boolean isfile;
    public boolean islink;

    public int size;

    public int user_perms;
    public int group_perms;
    public int other_perms;

    public String target;
    public String owner;
    public String group;

    public long atime;
    public long mtime;
    public long ctime;

    FileStat(String name) {
        this.name = name;
    }

    public static boolean isReadable(int perms) {
        return (perms & READ) > 0;
    }
    public static boolean isWritable(int perms) {
        return (perms & WRITE) > 0;
    }
    public static boolean isExecutable(int perms) {
        return (perms & EXEC) > 0;
    }

    public long getModifiedDate() {
        return mtime*1000;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(isdir ? 'd' : '-');
        buf.append(isReadable(user_perms) ? 'r' : '-');
        buf.append(isWritable(user_perms) ? 'w' : '-');
        buf.append(isExecutable(user_perms) ? 'x' : '-');
        buf.append(isReadable(group_perms) ? 'r' : '-');
        buf.append(isWritable(group_perms) ? 'w' : '-');
        buf.append(isExecutable(group_perms) ? 'x' : '-');
        buf.append(isReadable(other_perms) ? 'r' : '-');
        buf.append(isWritable(other_perms) ? 'w' : '-');
        buf.append(isExecutable(other_perms) ? 'x' : '-');
        buf.append((' '));
        buf.append(owner!=null ? owner : "?");
        buf.append((' '));
        buf.append(group!=null ? group : "?");
        buf.append((' '));
        buf.append(size);
        buf.append((' '));
        buf.append(m_formatter.format(new Date(getModifiedDate())));
        buf.append((' '));
        buf.append(name);
        if(isdir) buf.append('/');
        if (target != null) {
        	buf.append(" -> ").append(target);
        }
        return buf.toString();
    }
}
