package osgi.common.services.test;

import javax.inject.Named;

@Named
public class CC {

    public void print() {
        System.out.println("cc print");
    }

}
