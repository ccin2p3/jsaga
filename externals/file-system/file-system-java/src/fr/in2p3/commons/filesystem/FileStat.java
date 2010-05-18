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

    public String owner;
    public String group;

    public long atime;
    public long mtime;
    public long ctime;

    public FileStat(String name) {
        this.name = name;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(isdir ? 'd' : '-');
        buf.append((user_perms&READ)>0 ? 'r' : '-');
        buf.append((user_perms&WRITE)>0 ? 'w' : '-');
        buf.append((user_perms&EXEC)>0 ? 'x' : '-');
        buf.append((group_perms&READ)>0 ? 'r' : '-');
        buf.append((group_perms&WRITE)>0 ? 'w' : '-');
        buf.append((group_perms&EXEC)>0 ? 'x' : '-');
        buf.append((other_perms&READ)>0 ? 'r' : '-');
        buf.append((other_perms&WRITE)>0 ? 'w' : '-');
        buf.append((other_perms&EXEC)>0 ? 'x' : '-');
        buf.append((' '));
        buf.append(owner!=null ? owner : "?");
        buf.append((' '));
        buf.append(group!=null ? group : "?");
        buf.append((' '));
        buf.append(size);
        buf.append((' '));
        buf.append(m_formatter.format(new Date(mtime)));
        buf.append((' '));
        buf.append(name);
        if(isdir) buf.append('/');
        return buf.toString();
    }
}
