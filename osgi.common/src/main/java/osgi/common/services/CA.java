package osgi.common.services;

import javax.inject.Inject;
import javax.inject.Named;

@Named("CA")
public class CA {

    @Inject
    CB cb;

    @Inject
    CC cc;

    public void print() {
        System.out.println("===============ca print==================");
        cb.print();
        cc.print();
        System.out.println("===============ca print end==================");
    }

}
