import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class Main {
    public static void main(String[] args) {
        //Viewer2D.exec(lectureImage("D:\\Desktop\\lenaB.png"));
        //Viewer2D.exec(filtreMedian(lectureImage("D:\\Desktop\\lenaB.png")));
        histogramme(lectureImage("Images\\motos\\001.jpg"));
    }

    public static Image lectureImage(String lien){
        return ImageLoader.exec(lien);
    }

    public static Image filtreMedian(Image img) {
        ByteImage resultat = new ByteImage(img);
        ArrayList<Integer> mediane = new ArrayList<Integer>();
        for (int x = 1; x < img.getXDim() -1; x++) {
            for (int y = 1; y < img.getYDim() -1; y++) {
                for (int z = 0; z < img.getBDim(); z++) {
                    mediane.add(img.getPixelXYBByte(x, y, z));
                    mediane.add(img.getPixelXYBByte(x +1, y, z));
                    mediane.add(img.getPixelXYBByte(x +1, y +1, z));
                    mediane.add(img.getPixelXYBByte(x - 1, y -1, z));
                    mediane.add(img.getPixelXYBByte(x  -1, y, z));
                    mediane.add(img.getPixelXYBByte(x, y -1, z));
                    mediane.add(img.getPixelXYBByte(x -1, y +1, z));
                    mediane.add(img.getPixelXYBByte(x + 1, y - 1, z));
                    mediane.add(img.getPixelXYBByte(x, y + 1, z));
                    Collections.sort(mediane);
                    int color;
                    if (mediane.size() % 2 == 1) {
                        color = mediane.get(mediane.size()/2);
                    } else {
                        color = (mediane.get(3) + mediane.get(4) + mediane.get(5)) / 3;
                    }
                    resultat.setPixelXYBByte(x, y, z, color);
                    mediane.clear();
                }
            }
        }
        return resultat;
    }

    /// Histogramme
    public static double[][] histogramme(Image img) {

        double[][] histo = new double[img.getBDim()][256];

        for(int i = 0; i < histo.length; i++) {
            for (int y = 0 ; y < histo[i].length ; y++)
            histo[i][y] = 0;
        }

        for(int x = 0; x < img.getXDim() ; x++) {
            for(int y = 0; y < img.getYDim() ;  y++) {
                for(int z = 0 ; z < img.getBDim() ; z++){
                    int valeur = img.getPixelXYBByte(x, y, z);
                    histo[z][valeur] += 1;
                }
            }
        }
        try {
            for(int i = 0; i < img.getBDim() ; i++)
            HistogramTools.plotHistogram(histo[i]);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'affichage : " + e);
        }
        return histo;
    }
}