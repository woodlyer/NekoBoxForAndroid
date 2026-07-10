package io.nekohasekai.sagernet.fmt.gost;

import io.nekohasekai.sagernet.fmt.AbstractBean;

public class GostBean extends AbstractBean {
    public String protocol = "socks5";
    public String username = "";
    public String password = "";
    public String customArgs = "";

    @Override
    public void initializeDefaultValues() {
        super.initializeDefaultValues();
        if (protocol == null) protocol = "socks5";
        if (username == null) username = "";
        if (password == null) password = "";
        if (customArgs == null) customArgs = "";
    }
}
