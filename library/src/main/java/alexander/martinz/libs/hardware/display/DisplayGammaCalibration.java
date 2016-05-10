/*
 * Copyright (C) 2013 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package alexander.martinz.libs.hardware.display;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;

import alexander.martinz.libs.hardware.R;
import alexander.martinz.libs.hardware.utils.HwIoUtils;
import alexander.martinz.libs.hardware.utils.HwUtils;
import at.amartinz.execution.RootShell;

public class DisplayGammaCalibration {
    public static final String TAG = DisplayGammaCalibration.class.getSimpleName();

    private String[] paths;
    private String[] descriptors;
    private int max;
    private int min;

    public DisplayGammaCalibration(Context context) {
        final Resources res = context.getResources();
        final String[] paths = res.getStringArray(R.array.hardware_display_gamma_calibration_paths);
        final String[] descs = res.getStringArray(R.array.hardware_display_gamma_calibration_descs);
        final String[] maxs = res.getStringArray(R.array.hardware_display_gamma_calibration_max);
        final String[] mins = res.getStringArray(R.array.hardware_display_gamma_calibration_min);

        String[] splitted;
        final int length = paths.length;
        for (int i = 0; i < length; i++) {
            // split it
            splitted = paths[i].split(",");
            boolean exists = false;
            for (final String path : splitted) {
                // if the file exists, set up the values
                if (HwIoUtils.fileExists(path)) {
                    // and get out of here to continue
                    exists = true;
                    break;
                }
            }

            // if the controls exist, set up the values and end searching
            if (exists) {
                this.paths = splitted;
                // maximum and minimum
                max = HwUtils.tryParseInt(maxs[i]);
                min = HwUtils.tryParseInt(mins[i]);
                // descriptors
                descriptors = descs[i].split(",");
                // get out of here finally
                break;
            }
        }
    }

    public boolean isSupported() { return paths != null && HwIoUtils.fileExists(paths[0]); }

    public int getMaxValue(final int control) { return max; }

    public int getMinValue(final int control) { return min; }

    public String getCurGamma(final int control) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paths.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(HwIoUtils.readOneLine(paths[i]));
        }
        return sb.toString();
    }

    public void setGamma(final int control, final String gamma) {
        if (paths.length <= 0) {
            return;
        }

        final String[] split = gamma.split(" ");
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paths.length; i++) {
            sb.append(String.format("echo \"%s\" > %s;", split[i], paths[i]));
        }
        RootShell.fireAndForget(sb.toString());
    }

    public String[] getDescriptors() { return descriptors; }

    // TODO: update if needed, return 1 for now
    public int getNumberOfControls() {
        return 1;
    }

    @Nullable public String[] getPaths(final int control) { return paths; }
}
