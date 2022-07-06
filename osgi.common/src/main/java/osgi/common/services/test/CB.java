package osgi.common.services.test;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class CB {

    @Inject
    CC cc;

    public void print() {
        System.out.println("=========cb cc.print() ========");
        cc.print();
        System.out.println("=========cb cc.print() end ========");
    }

}
