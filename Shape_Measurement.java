// Referência usado
// http://www.imagemet.com/WebHelp6/Content/PnPParameters/Measure_Shape_Parameters.htm

import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.filter.*;

public class Shape_Measurement implements PlugInFilter {
        ImagePlus img;

        @Override
    public int setup(String arg, ImagePlus imp) {
        img = imp;
        ImageConverter ic = new ImageConverter(imp);
        ic.convertToGray8();
        return DOES_ALL;
    }

        @Override
        public void run(ImageProcessor arg) {

                GenericDialog gd = new GenericDialog("Vetor de caracteristicas", IJ.getInstance());
        
        gd.addMessage("Diametro efetivo(Heywood) = " + diameter() + "\n" +
                                  "Circularidade = " + formFactor() + "\n" +
                                  "Arredondamento = " + roundness() + "\n" +
                                  "Compactacao = " + compactness() + "\n" +
                                  "Razao de Raio = " + aspectRatio());
        gd.showDialog();

        }


        // Area (Área)
        // The Area is calculated from the shapes periphery, i.e. the closed polygon that surrounds the feature.
        public double area() {
                double area = 0.0;
                try {
                        ImageProcessor output = img.getProcessor();

                        for(int x = 0; x < output.getWidth(); x++)
                                for(int y = 0; y < output.getHeight(); y++)
                                        if(output.getPixel(x, y) != 0)
                                                area++;
                }
                catch(Exception e){
                        String err = "Erro no cálculo da área da imagem \n" + e.toString();
                        IJ.log(err);
                }
                return area;
        }

        // Perimeter (Perímetro)
        // A perimeter is a path that surrounds a two-dimensional shape.
        public double perimeter() {
                double perimeter = 0.0;
        try {
            ImageProcessor output = img.getProcessor();

            for(int x = 2; x < output.getWidth(); x++)
                    for(int y = 0; y < output.getHeight(); y++) {
                        if(output.getPixel(x, y) != 0 && ((x-1) > 0) && ((y-1) > 0) && (((output.getPixel(x+1, y) == 0) || (output.getPixel(x-1, y) == 0) || (output.getPixel(x, y+1) == 0) || (output.getPixel(x, y-1) == 0))))
                                perimeter++;
                }
        }
        catch (Exception e){
            String err = "Erro no cálculo do perímetro da imagem \n" + e.toString();
            IJ.log(err);
        }
        return perimeter;
        }

        // Euclidian Distance (Distância Euclidiana)
        // the Euclidean distance is the "ordinary" distance between two points.
        public double euclidianDistance(int x1, int y1, int x2, int y2) {
                return Math.sqrt(Math.pow((x2-x1),2) + Math.pow(y2-y1,2));
        }

        // Length (Maior Eixo)
        // Length is defined as the longest cord along the angle Q given by the moment's axis to the x-axis.
        public double length() {
                double length = 0.0;
                int x1, y1, x2, y2;
                int perimeter = (int)perimeter();
        try {
                        ImageProcessor output = img.getProcessor();

            int[][] pixelPerimeter = new int[2][perimeter];
            int k = 0;
            
            for (int x = 0; x < output.getWidth(); x++)
                                for (int y = 0; y < output.getHeight(); y++) {
                    if(output.getPixel(x, y) != 0 && ((x-1) > 0) && ((y-1) > 0) && ((output.getPixel(x+1, y)==0) || (output.getPixel(x-1, y)==0) || (output.getPixel(x, y+1)==0) || (output.getPixel(x, y-1)==0))) {
                                                pixelPerimeter[0][k] = x;
                                                pixelPerimeter[1][k] = y;
                                                k++;
                    }
                }

            double temp;
            for(int i = 0; i < perimeter; i++)
                for(int j = i+1; j < perimeter; j++) {
                    x1 = pixelPerimeter[0][i];
                    y1 = pixelPerimeter[1][i];
                    x2 = pixelPerimeter[0][j];
                    y2 = pixelPerimeter[1][j];    
                    temp = euclidianDistance(x1,y1,x2,y2);
                    if(temp > length)
                        length = temp;
                }
        }
        catch(Exception e){
            String err = "Erro no cálculo do maior eixo da imagem \n" + e.toString();
            IJ.log(err);
        }
        return length;
        }

        // // Breadth (Menor Eixo)
        // Breadth (or width) is defined as the longest cord perpendicular to the angle Q given by the moments axis to the x-axis.
        public double breadth() {
                double breadth = 0.0;
        double temp, lengthCoef = 0.0, breadthCoef = 0.0;
        int x1, y1, x2, y2;
        int perimeter = (int)perimeter();
        double [][] lengthCoord = new double[2][2];
        
        try {
                ImageProcessor output = img.getProcessor();
                int[][] pixelPerimeter = new int[2][perimeter];
            int k = 0;
            
            for (int x = 0; x < output.getWidth(); x++)
                                for (int y = 0; y < output.getHeight(); y++) {
                    if(output.getPixel(x, y) != 0 && ((x-1) > 0) && ((y-1) > 0) && ((output.getPixel(x+1, y)==0) || (output.getPixel(x-1, y)==0) || (output.getPixel(x, y+1)==0) || (output.getPixel(x, y-1)==0))) {
                                                pixelPerimeter[0][k] = x;
                                                pixelPerimeter[1][k] = y;
                                                k++;
                    }
                }
            
            double length = 0.0;
            for(int i = 0; i < perimeter; i++)
                for(int j = i+1; j < perimeter; j++) {
                    x1 = pixelPerimeter[0][i];
                    y1 = pixelPerimeter[1][i];
                    x2 = pixelPerimeter[0][j];
                    y2 = pixelPerimeter[1][j];
                    temp = euclidianDistance(x1,y1,x2,y2);
                    if(temp > length) {
                        length = temp;
                        lengthCoord[0][0] = x1;
                        lengthCoord[1][0] = y1;
                        lengthCoord[0][1] = x2;
                        lengthCoord[1][1] = y2;
                        
                        if (x2-x1 == 0)
                                lengthCoef = 0;
                        else
                                lengthCoef = (double)(y2-y1)/(x2-x1);
                    }
                }
            for(int i = 0; i < perimeter; i++)
                for(int j = i+1; j < perimeter; j++) {
                    x1 = pixelPerimeter[0][i];
                    y1 = pixelPerimeter[1][i];
                    x2 = pixelPerimeter[0][j];
                    y2 = pixelPerimeter[1][j];

                    if((x2-x1) == 0)
                            breadthCoef = 0;
                    else
                            breadthCoef = (double)(y2-y1)/(x2-x1);

                            double parallel = (double)(lengthCoef * breadthCoef);
                    if((parallel >= -1.10) && (parallel <= -0.90)) {
                        temp = euclidianDistance(x1,y1,x2,y2);
                        if(temp > breadth)
                            breadth = temp;
                    }
                }
        // }
        }
        catch(Exception e){
            String err = "Erro no cálculo do menor eixo da imagem \n" + e.toString();
            IJ.log(err);
        }
        return breadth;
        }

        // Diameter (Diâmetro Efetivo)
        // The diameter (or Heywood diameter) is expressed as the diameter of a circle having an area equivalent to the shape's area.
        public double diameter() {
                double diameter = 0.0;
        try {
            diameter = 2 * Math.sqrt((double)area()/Math.PI);
        }
        catch(Exception e){
            String err = "Erro no cálculo do diâmetro efetivo da imagem \n" + e.toString();
            IJ.log(err);
        }

        return diameter;
        }

        // Form Factor (Circularidade)
        // Form Factor provides a measure that describes the Shape of a feature.
        public double formFactor() {
                double formFactor = 0.0;
        try {
            formFactor = (4 * Math.PI*(double)area()) / (Math.pow((double)perimeter(), 2));
        }
        catch(Exception e){
            String err = "Erro no cálculo da circularidade da imagem \n" + e.toString();
            IJ.log(err);
        }
        return formFactor;
        }

        // Roundness (Arredondamento) 
        // Roundness describes the Shape's resemblance to a circle. The roundness factor of a Shape will approach 1.0 the closer the Shape resembles a circle.
        public double roundness() {
        double roundness = 0.0;
        
        try {
            roundness = ((4 * (double)area())/(Math.PI * Math.pow(length(), 2)));
        }
        catch(Exception e){
            String err = "Erro no cálculo do arredondamento da imagem \n" + e.toString();
            IJ.log(err);
        }
        return roundness;
    }

        // Compactness (Compactação)
        // Compactness is a measure expressing how compact a feature is. A circle will have a compactness of 1.0, a squares compactness is 1.1284, whereas elongated and irregular shapes results in values less than 1.0.
        public double compactness() {
        double compactness = 0.0;
        try {
            compactness = diameter()/length();
        }
        catch(Exception e){
            String err = "Erro no cálculo da compactação da imagem \n" + e.toString();
            IJ.log(err);
        }
        return compactness;
    }

        // Aspect Ratio (Razão de Raio)
        // Aspect Ratio is the aspect ration defined as Length over Breadth. The aspect ration will from this definition always be greater than or equal to 1.0. The aspect ratio of both a circle and square is 1.0, whereas other shapes will have a value less than 1.0.
        public double aspectRatio() {
        double aspectRatio = 0.0;
        try {
            aspectRatio = (double)length()/(double)breadth();
        }
        catch(Exception e){
            String err = "Erro no cálculo da razão de raio da imagem \n" + e.toString();
            IJ.log(err);
        }
        return aspectRatio;
    }

}