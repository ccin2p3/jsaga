package fr.in2p3.jsaga.helpers;

import java.io.*;
import java.util.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ASCIITableFormatter
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   3 ao�t 2007
* ***************************************************
* Description:                                      */
/**
 * Format appended data as an ASCII table.
 * Notes: the number of columns can vary, then missing columns are ignored and additional columns are displayed unformatted.
 */
public class ASCIITableFormatter {
    private int SCREEN_WIDTH = 79;
    private int[] m_maxWidth;
    private String[] m_headers;
    private List m_data;

    public ASCIITableFormatter(String[] headers) {
        m_maxWidth = new int[headers.length];
        for (int i=0; i<headers.length; i++) {
            m_maxWidth[i] = (headers[i]!=null ? headers[i].length() : 0);
        }
        m_headers = headers;
        m_data = new ArrayList();
    }

    public ASCIITableFormatter(int maxColumns) {
        m_maxWidth = new int[maxColumns];
        for (int i=0; i<m_maxWidth.length; i++) {
            m_maxWidth[i] = 0;
        }
        m_headers = null;
        m_data = new ArrayList();
    }

    public void append(String[] row) throws Exception {
        if (row != null) {
            if (row.length > m_maxWidth.length) {
                throw new Exception("Number of columns out of bounds: "+row.length);
            }
            // update maxLength
            for (int i=0; i<row.length; i++) {
                if (row[i]!=null && m_maxWidth[i]<row[i].length()) {
                    m_maxWidth[i] = row[i].length();
                }
            }
            m_data.add(row);
        }
    }

    public void dump(OutputStream stream) throws IOException {
        // reduce column widths
        int[] indices = sortIndicesByMaxWidth();
        int totalWidth;
        for (int i=0; i<indices.length && (totalWidth=getTotalWidth())>SCREEN_WIDTH; i++) {
            int currentMaxWidth = m_maxWidth[indices[i]];
            int nextMaxWidth = i<indices.length-1 ? m_maxWidth[indices[i+1]] : 0;
            int newWidth = currentMaxWidth - (totalWidth - SCREEN_WIDTH);
            if (newWidth <= nextMaxWidth) {
                newWidth = nextMaxWidth + 1;
            }
            m_maxWidth[indices[i]] = newWidth;
        }

        // dump
        PrintStream out = new PrintStream(stream);
        for (int i=0; m_headers!=null && i<m_headers.length; i++) {
            if(i>0) out.print(" | ");
            if (i < m_maxWidth.length) {
                int space = m_maxWidth[i] - m_headers[i].length();
                int indent = space / 2;
                fill(' ', indent, out);
                out.print(m_headers[i]);
                fill(' ', space - indent, out);
            } else {
                out.print(m_headers[i]);                
            }
        }
        out.println();
        for (int i=0; i<m_maxWidth.length; i++) {
            if(i>0) out.print("-+-");
            fill('-', m_maxWidth[i], out);
        }
        out.println();
        for (Iterator it=m_data.iterator(); it.hasNext(); ) {
            String[] row = (String[]) it.next();
            int heightMax = getHeightMax(row);
            for (int h=0; h<heightMax; h++) {
                for (int i=0; row!=null && i<row.length; i++) {
                    if(i>0) out.print(" | ");
                    if (row[i] != null) {
                        int start = h*m_maxWidth[i];
                        int end = min((h+1)*m_maxWidth[i], row[i].length());
                        String lineOfRow = row[i]!=null && start<row[i].length()
                                ? row[i].substring(start, end)
                                : null;
                        if (lineOfRow != null) {
                            out.print(lineOfRow);
                            if (i < m_maxWidth.length) {
                                fill(' ', m_maxWidth[i]-lineOfRow.length(), out);
                            }
                        } else {
                            fill(' ', m_maxWidth[i], out);
                        }
                    } else {
                        fill(' ', m_maxWidth[i], out);
                    }
                }
                for (int i=row.length; i<m_maxWidth.length; i++) {
                    if(i>0) out.print(" | ");
                    fill(' ', m_maxWidth[i], out);
                }
                out.println();
            }
        }
    }

    private int[] sortIndicesByMaxWidth() {
        int[] indices = new int[m_maxWidth.length];
        for (int i=0; i<indices.length; i++) {
            indices[i] = getIndiceWithMaxWidth(indices);
        }
        return indices;
    }
    private int getIndiceWithMaxWidth(int[] indices) {
        int widthMax = -1;
        int indiceMax = -1;
        for (int i=0; i<m_maxWidth.length; i++) {
            if (m_maxWidth[i]>widthMax && !contains(indices,i)) {
                widthMax = m_maxWidth[i];
                indiceMax = i;
            }
        }
        return indiceMax;
    }
    private boolean contains(int[] array, int value) {
        for (int i=0; i<array.length && array[i]!=0; i++) {
            if (array[i] == value) {
                return true;
            }
        }
        return false;
    }

    private int getTotalWidth() {
        int totalWidth = (m_maxWidth.length-1)*3;
        for (int i=0; i<m_maxWidth.length; i++) {
            totalWidth += m_maxWidth[i];
        }
        return totalWidth;
    }

    private int getHeightMax(String[] row) {
        int heightMax = 1;
        for (int i=0; row!=null && i<row.length; i++) {
            if (row[i] != null) {
                int height = (row[i].length() + m_maxWidth[i] - 1) / m_maxWidth[i];
                if (height > heightMax) {
                    heightMax = height;
                }
            }
        }
        return heightMax;
    }

    private int min(int v1, int v2) {
        return (v1<v2 ? v1 : v2);
    }

    private void fill(char c, int len, PrintStream out) throws IOException {
        for (int i=0; i<len; i++) {
            out.print(c);
        }
    }
}
