package com.absoluteguitar.mob;

/**
 * Created by isuru on 5/28/15.
 */
public class AbsoluteGuitarFFT {



    // Taken from http://www.ddj.com/cpp/199500857
    // which took it from Numerical Recipes in C++, p.513

    public void doFFTInternal( double [] data, int nn ) {

        int n, mmax, m, j, istep, i;
        double wtemp, wr, wpr, wpi, wi, theta;
        double tempr, tempi;

        // reverse-binary reindexing
        n = nn<<1;
        j=1;
        for (i=1; i<n; i+=2) {
            if (j>i) {
                swap( data, j-1, i-1 );
                swap( data, j, i);
            }
            m = nn;
            while (m>=2 && j>m) {
                j -= m;
                m >>= 1;
            }
            j += m;
        };


        // here begins the Danielson-Lanczos section
        mmax = 2;
        while (n>mmax) {
            istep = mmax<<1;
            theta = -(2*Math.PI/mmax);
            wtemp = Math.sin(0.5*theta);
            wpr = -2.0*wtemp*wtemp;
            wpi = Math.sin(theta);
            wr = 1.0;
            wi = 0.0;
            for (m=1; m < mmax; m += 2) {
                for (i=m; i <= n; i += istep) {
                    j=i+mmax;
                    tempr = wr*data[j-1] - wi*data[j];
                    tempi = wr * data[j] + wi*data[j-1];


                    data[j-1] = data[i-1] - tempr;
                    data[j] = data[i] - tempi;
                    data[i-1] += tempr;
                    data[i] += tempi;
                }
                wtemp=wr;
                wr += wr*wpr - wi*wpi;
                wi += wi*wpr + wtemp*wpi;
            }
            mmax=istep;
        }
    }


    private void swap( double [] data, int x, int y ) {
        double temp = data[x];
        data[x] = data[y];
        data[y] = temp;
    }
}
