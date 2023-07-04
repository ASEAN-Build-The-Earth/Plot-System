/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.alpsbte.plotsystem.core.system.plot;

public enum PlotType {

    FOCUS_MODE(0),
    LOCAL_INSPIRATION_MODE(1),
    CITY_INSPIRATION_MODE(2);

    final int id;

    PlotType(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }

    // Returns true, if the plot type only contains environment around the plot.
    public boolean hasEnvironment(){
        return id == 1 || id == 2;
    }

    // Returns true, if the plot type only contains one plot per world.
    public boolean hasOnePlotPerWorld(){
        return id == 0 || id == 1;
    }

    public static PlotType byId(int id){
        for(PlotType plotType : values())
            if(plotType.getId() == id)
                return plotType;

        return null;
    }
}
