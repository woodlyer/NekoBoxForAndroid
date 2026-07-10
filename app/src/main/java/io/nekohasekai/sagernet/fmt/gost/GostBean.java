package io.nekohasekai.sagernet.fmt.gost;

import androidx.annotation.NonNull;

import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;

import org.jetbrains.annotations.NotNull;

import io.nekohasekai.sagernet.fmt.AbstractBean;
import io.nekohasekai.sagernet.fmt.KryoConverters;

public class GostBean extends AbstractBean {
    public String protocol = "socks5";
    public String username = "";
    public String password = "";
    public String customArgs = "";
    public String extraHeaders = "";
    public String sni = "";
    public String certificates = "";
    public Integer insecureConcurrency = 0;
    public Boolean sUoT = false;

    @Override
    public void initializeDefaultValues() {
        super.initializeDefaultValues();
        if (protocol == null) protocol = "socks5";
        if (username == null) username = "";
        if (password == null) password = "";
        if (customArgs == null) customArgs = "";
        if (extraHeaders == null) extraHeaders = "";
        if (sni == null) sni = "";
        if (certificates == null) certificates = "";
        if (insecureConcurrency == null) insecureConcurrency = 0;
        if (sUoT == null) sUoT = false;
    }

    @Override
    public void serialize(ByteBufferOutput output) {
        output.writeInt(1); // version
        super.serialize(output);
        output.writeString(protocol);
        output.writeString(username);
        output.writeString(password);
        output.writeString(customArgs);
        output.writeString(extraHeaders);
        output.writeString(sni);
        output.writeString(certificates);
        output.writeInt(insecureConcurrency);
        output.writeBoolean(sUoT);
    }

    @Override
    public void deserialize(ByteBufferInput input) {
        int version = input.readInt();
        super.deserialize(input);
        protocol = input.readString();
        username = input.readString();
        password = input.readString();
        customArgs = input.readString();
        extraHeaders = input.readString();
        sni = input.readString();
        certificates = input.readString();
        insecureConcurrency = input.readInt();
        sUoT = input.readBoolean();
    }

    @NotNull
    @Override
    public GostBean clone() {
        return KryoConverters.deserialize(new GostBean(), KryoConverters.serialize(this));
    }

    public static final Creator<GostBean> CREATOR = new CREATOR<GostBean>() {
        @NonNull
        @Override
        public GostBean newInstance() {
            return new GostBean();
        }

        @Override
        public GostBean[] newArray(int size) {
            return new GostBean[size];
        }
    };
}
