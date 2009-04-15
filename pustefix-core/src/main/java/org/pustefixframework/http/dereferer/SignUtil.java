/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pustefixframework.http.dereferer;

import java.util.Random;

import de.schlund.pfixxml.util.MD5Utils;

public abstract class SignUtil {
    
    private final static String SIGN_KEY;
    
    static {
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 32; i++) {
            sb.append(random.nextInt(10));
        }
        SIGN_KEY = sb.toString();
    }
    
    public static String getSignature(String str, long timeStamp) {
        return MD5Utils.hex_md5(str + timeStamp + SIGN_KEY, "utf8");
    }
    
    public static boolean checkSignature(String str, long timeStamp, String signature) {
        return signature.equals(getSignature(str, timeStamp));
    }
    
    public static String getFakeSessionIdArgument(String sessionId) {
        // if the session id is empty, there is no fake session id
        if (sessionId == null || sessionId.trim().length() == 0) {
            return "";
        }
        // if the session id does not contain a jvm route, a fake session id
        // makes no sense
        int dotPos = sessionId.lastIndexOf('.');
        if (dotPos == -1) {
            return "";
        }
        // otherwise, keep the part of the session id after the last dot
        return ";jsessionid=nosession" + sessionId.substring(dotPos);
    }
    
    public static String getTimeStamp() {
        return String.valueOf(System.currentTimeMillis());
    }
    
}
