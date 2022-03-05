import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

import java.io.File;
import java.io.IOException;
import java.sql.Array;
import java.util.*;

public class Main {

    private static Map mapTriee = new TreeMap<Double, Image>(); //Range les images grâce à leurs similarités à l'image donnée

    private static ArrayList<File> images = new ArrayList<File>(); //Listes de toutes les images en bdd à traiter
    public static void main(String[] args) {
        /**
         * Traite les informations de l'utilisateurs et simule la BDD d'images que l'on possède
         */
        Scanner entree = new Scanner(System.in);
        String dossierImage;
        String imageRef;
        System.out.println("Veuillez-entrer le lien absolu du dossier contenant les images :");
        dossierImage = entree.next();
        System.out.println(dossierImage);
        System.out.println("Veuillez-entrer le lien absolu de l\'image de référence :");
        imageRef = entree.next();


        /**
         * Traitement de la première image de l'utilisateur en amont des images de la BBD
         */
        Image testRef = lectureImage(imageRef);
        Image testMedianRef = filtreMedian(testRef);

        double[][] ref0 = histogramme(testMedianRef);
        double[][] ref1 = discretiserHistogramme(ref0);
        //double[][] ref2 = discretiserHistogramme(ref1); // Meilleur resultat avec 2 & 3 discretisations
        //double[][] ref3 = discretiserHistogramme(ref2);
        //double[][] ref4 = discretiserHistogramme(ref3);
        //double[][] ref5 = discretiserHistogramme(ref4);
        double[][] normRef = normaliserHistogramme(ref1, testRef);
        //mapTriee.put(similariteHistogramme(normRef, normRef), testRef);

        /*try {
            for(int i = 0; i < ref5.length ; i++)
                HistogramTools.plotHistogram(ref5[i]);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'affichage : " + e);
        }*/
        /**
         * Traitement des images de la BDD une à une de la même manière que l'image de référence
         */
        File repertoire = new File(dossierImage);
        File[] listeFile = repertoire.listFiles();
        for (int i = 0; i < listeFile.length; ++i) {
            if (imageRef.equals(listeFile[i].getAbsolutePath())) {
                continue;
            }
            Image test = lectureImage(listeFile[i].getAbsolutePath());
            Image testMedian = filtreMedian(test);
            double[][] ok = histogramme(testMedian);
            double[][] ok2 = discretiserHistogramme(ok);
            //double[][] ok3 = discretiserHistogramme(ok2);
            //double[][] ok4 = discretiserHistogramme(ok3);
            //double[][] ok5 = discretiserHistogramme(ok4);
            //double[][] ok6 = discretiserHistogramme(ok5);
            double[][] norm = normaliserHistogramme(ok2, test);
            mapTriee.put(similariteHistogramme(norm, normRef), test);
        }
        Set<Double> keys = mapTriee.keySet(); // Return en Set qu'on ne peut qu'itérer = pose problème lorsque qu'on récupérer que les 10 première images
        ArrayList<Double> ok = new ArrayList<>(); //Ranger les keys des images de la tree dans un Array qu'on peut intérer
        for (Double key : keys) {
            ok.add(key);
        }
        for(int i = 0 ; i < 10 ; i++){
            Viewer2D.exec((Image) mapTriee.get(ok.get(i))); //Affichage des 10 premières images qui correspondent le plus avec l'img de Ref
        }
    }

    /**
     *
     * @param lienImage
     *      Permet de charger une image sur laquelle on va travailler
     * @return Image chargée par la classe ImageLoader
     */
    public static Image lectureImage(String lienImage){
        return ImageLoader.exec(lienImage);
    }

    /**
     *
     * @param img Une image chargée
     *      Cette fonction permet de débruiter une image avec l'algorithme du filtre médian + efficace que le filtre moyen
     *      Le contour sur 1 pixel de taille de l'image n'est pas débruité pour le bien de l'algorithme
     * @return L'image en plus net
     */
    public static Image filtreMedian(Image img) {
        ByteImage resultat = new ByteImage(img);
        ArrayList<Integer> mediane = new ArrayList<Integer>();
        for (int x = 1; x < img.getXDim() -1; x++) {
            for (int y = 1; y < img.getYDim() -1; y++) {
                for (int z = 0; z < img.getBDim(); z++) {
                    //Range les 8 pixels autour du pixel à traiter dans le traitement de la médianne || Donné dans la fonction de base
                    mediane.add(img.getPixelXYBByte(x, y, z));
                    mediane.add(img.getPixelXYBByte(x +1, y, z));
                    mediane.add(img.getPixelXYBByte(x +1, y +1, z));
                    mediane.add(img.getPixelXYBByte(x - 1, y -1, z));
                    mediane.add(img.getPixelXYBByte(x  -1, y, z));
                    mediane.add(img.getPixelXYBByte(x, y -1, z));
                    mediane.add(img.getPixelXYBByte(x -1, y +1, z));
                    mediane.add(img.getPixelXYBByte(x + 1, y - 1, z));
                    mediane.add(img.getPixelXYBByte(x, y + 1, z));
                    Collections.sort(mediane); //Trie nécessaire pour avoir la médiane des 9 points
                    int color;
                    //Mediane consiste à diviser en 2 groupes de valeurs le plus égaux possible ; Traitement nécessaire si la liste est paire
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

    /**
     *
     * @param img Prend l'image sur laquelle on va construire
     *            Fonction qui s'adapte, peut construire l'histogramme d'une image en couleur RGB ou même en gris
     * @return Histogramme sur toutes les dimensions de couleurs de l'image
     */
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
        //Affichage des histogrammes
//        try {
//            for(int i = 0; i < img.getBDim() ; i++)
//            HistogramTools.plotHistogram(histo[i]);
//        } catch (IOException e) {
//            System.err.println("Erreur lors de l'affichage : " + e);
//        }
        return histo;
    }

    /**
     *
     * @param histogramme
     *      Fonction qui permet de discréstiser un histogramme ; Simplifie le traitement de l'histogramme d'une image
     *      Problème de cette fonction, c'est que l'on ne peut pas la répéter, on doit refaire appel à celle-ci pour discrétiser une seconde fois ect
     *      1ère discrétisation 128 barres de 2 valeurs -> 2ème 64 barres de 4 -> 3ème 32 barres de 8 -> 4ème 16 barres de 16 valeurs -> 5ème 8 barres de 32 valeurs
     *      Affecte la précision de la l'algorithme de reconnaissance d'images similaires
     * @return Histogramme "simplifié" par 2
     */
    public static double[][] discretiserHistogramme(double[][] histogramme){
        double[][] nouveauHistogramme = new double[histogramme.length][histogramme[0].length / 2];
        for(int i = 0 ; i < nouveauHistogramme.length; i++){
                for(int y = 0 ; y < nouveauHistogramme[0].length ; y++){
                    nouveauHistogramme[i][y] = histogramme[i][y * 2] + histogramme[i][y * 2 + 1];
                }
        }

        return nouveauHistogramme;
    }

    /**
     *
     * @param histogramme de l'image
     * @param img pour récupérer ses dimensions
     *      Après discrétisation, il est nécessaire d'obtenir un diagramme fait de pourcentages
     *            -> Savoir quel niveau de couleur est le plus représenté dans l'image en pourcentage
     * @return Un histogramme en pourcentage
     */
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

    /**
     *
     * @param h1 Histogramme normalisé de l'image à traiter
     * @param h2 Histogramme normalisé de l'image de référence : Celle que donne l'utilisateur
     *           Résultat : Plus le résultat est petit + l'image est resemble à la première
     * @return Double représentation une similarité entre 2 images
     */
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