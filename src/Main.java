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
        Image test = lectureImage("D:\\Desktop\\lenaB.png");
        Image testMedian = filtreMedian(test);

        double[][] histogramme = histogramme(testMedian);
        double[][] dHisto = discretiserHistogramme(histogramme);
        double[][] dHisto1 = discretiserHistogramme(dHisto);
        double[][] dHisto2 = discretiserHistogramme(dHisto1);
        double[][] dHisto3 = discretiserHistogramme(dHisto2);
        double[][] dHisto4 = discretiserHistogramme(dHisto3);
        double[][] norm = normaliserHistogramme(dHisto4, testMedian);
        //Viewer2D.exec(test);
        //Viewer2D.exec(testMedian);
        System.out.println(histogramme);

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
        //try {
        //    for(int i = 0; i < img.getBDim() ; i++)
        //    HistogramTools.plotHistogram(histo[i]);
        //} catch (IOException e) {
        //    System.err.println("Erreur lors de l'affichage : " + e);
        //}
        return histo;
    }

    public static double[][] discretiserHistogramme(double[][] histogramme){
        double[][] nouveauHistogramme = new double[histogramme.length][histogramme[0].length / 2];
        for(int i = 0 ; i < nouveauHistogramme.length; i++){
                for(int y = 0 ; y < nouveauHistogramme[0].length ; y++){
                    nouveauHistogramme[i][y] = histogramme[i][y * 2] + histogramme[i][y * 2 + 1];
                }
        }
        return nouveauHistogramme;
    }

    public static double[][] normaliserHistogramme(double [][] histogramme, Image img){
        int longueur = img.getXDim();
        int largeur = img.getYDim();
        int pixels = longueur * largeur;
        for(int i = 0 ; i < histogramme.length; i++){
            for(int y = 0 ; y < histogramme[0].length ; y++) {
                histogramme[i][y] = (histogramme[i][y] / pixels) * 100;
            }
        }
        return histogramme;
    }
    public static int similariteHistogramme(double[][] h1, double[][] h2){
        return 0;
    }
}