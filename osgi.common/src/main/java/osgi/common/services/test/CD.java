package osgi.common.services.test;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class CD {

    @Inject
    CA ca;

    @Inject
    CB cb;

    public void print() {
        System.out.println("===============cd print==================");
        ca.print();
        cb.print();
        System.out.println("===============cd print end==================");
    }

}
