import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner entree = new Scanner(System.in);
        String dossierImage;
        String imageRef;
        System.out.println("Veuillez-entrer le lien absolu du dossier contenant les images :");
        dossierImage = entree.next();
        System.out.println(dossierImage);
        System.out.println("Veuillez-entrer le lien absolu de l\'image de référence :");
        imageRef = entree.next();

        File repertoire = new File(dossierImage);
        File[] listeFile = repertoire.listFiles();
        for(int i = 0 ; i < listeFile.length ; i++){
            if(imageRef.equals(listeFile[i].getAbsolutePath())){
                listeFile[i].delete();
            }
            Image test = lectureImage(listeFile[i].getAbsolutePath());
            Image testMedian = filtreMedian(test);
            double[][] ok = histogramme(testMedian);
            double[][] ok2 = discretiserHistogramme(ok);
            double[][] ok3 = discretiserHistogramme(ok2);
            double[][] ok4 = discretiserHistogramme(ok3);
            double[][] norm = normaliserHistogramme(ok4, test);
            System.out.println(similariteHistogramme(norm, norm));
        }
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
//        try {
//            for(int i = 0; i < img.getBDim() ; i++)
//            HistogramTools.plotHistogram(histo[i]);
//        } catch (IOException e) {
//            System.err.println("Erreur lors de l'affichage : " + e);
//        }
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
    public static double similariteHistogramme(double[][] h1, double[][] h2) {
        double distanceR = 0;
        double distanceG = 0;
        double distanceB = 0;

        for (int i = 0; i < h1.length; i++) {
            for (int y = 0; y < h1[i].length; y++) {
                double h1BarreHauteur = h1[i][y];
                double h2BarreHauteur = h2[i][y];
                switch(i){
                    case (0):
                        distanceR+= Math.pow((h1BarreHauteur - h2BarreHauteur), 2);
                        break;
                    case (1):
                        distanceG+= Math.pow((h1BarreHauteur - h2BarreHauteur), 2);
                        break;
                    case (2):
                        distanceB+= Math.pow((h1BarreHauteur - h2BarreHauteur), 2);
                        break;
                }
            }
        }
        return (Math.sqrt(distanceR) + Math.sqrt(distanceG) + Math.sqrt(distanceB));
    }
}