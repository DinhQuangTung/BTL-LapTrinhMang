package Publisher;

import GlobalVariable.*;
import java.util.*;

public class Publisher implements Locations, Topics {
    static int createMode() {
        Random rand = new Random();
        return rand.nextInt(2);
    }

    public static void main(String[] args) throws Exception {
        int mode = createMode();
        System.out.println(mode);
    }
}
