package osgi.common.services;

import javax.inject.Named;

@Named
public class CC {

    public void print() {
        System.out.println("cc print");
    }

}
