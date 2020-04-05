package main;
//package lvl2advanced.p01gui.p01simple;

import lwjglutils.OGLBuffers;

/**
 * @author PGRF FIM UHK
 * @version 2.0
 * @since 2019-09-02
 */
public class GridFactory {

    /**
     * @param m počet vrcholů v řádku
     * @param n počet vrcholů ve sloupci
     * @return
     */
    public static OGLBuffers generateGrid(int m, int n) {
        float[] vb = new float[m * n * 2];
        int index = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                vb[index++] = j / (float) (m - 1);
                vb[index++] = i / (float) (n - 1);
            }
        }

        int[] ib = new int[(m - 1) * (n - 1) * 2 * 3];
        int index2 = 0;
        for (int i = 0; i < n - 1; i++) {
            int row = i * m;
            for (int j = 0; j < m - 1; j++) {
//                System.out.println(j + row);
//                System.out.println(j + 1 + row);
//                System.out.println(j + m + row);
//
//                System.out.println(j + m + row);
//                System.out.println(j + 1 + row);
//                System.out.println(j + m + 1 + row);

                ib[index2++] = j + row;
                ib[index2++] = j + 1 + row;
                ib[index2++] = j + m + row;

                ib[index2++] = j + m + row;
                ib[index2++] = j + 1 + row;
                ib[index2++] = j + m + 1 + row;
            }
        }

        OGLBuffers.Attrib[] attributes = {
                new OGLBuffers.Attrib("inPosition", 2)
        };
        return new OGLBuffers(vb, attributes, ib);
    }

//    public static void main(String[] args) {
//        GridFactory.generateGrid(4, 3);
//    }

}
