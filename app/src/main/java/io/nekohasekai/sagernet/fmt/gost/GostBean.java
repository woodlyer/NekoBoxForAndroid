package io.nekohasekai.sagernet.fmt.gost;

import androidx.annotation.NonNull;

import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;

import org.jetbrains.annotations.NotNull;

import io.nekohasekai.sagernet.fmt.AbstractBean;
import io.nekohasekai.sagernet.fmt.KryoConverters;

public class GostBean extends AbstractBean {
    public String customArgs = "";
    public String customConfigFileName = "kcp.json";
    public String customConfigFileContent = "";

    @Override
    public void initializeDefaultValues() {
        super.initializeDefaultValues();
        if (customArgs == null) customArgs = "";
        if (customConfigFileName == null) customConfigFileName = "kcp.json";
        if (customConfigFileContent == null) customConfigFileContent = "";
    }

    @Override
    public void serialize(ByteBufferOutput output) {
        output.writeInt(1); // version
        super.serialize(output);
        output.writeString(customArgs);
        output.writeString(customConfigFileName);
        output.writeString(customConfigFileContent);
    }

    @Override
    public void deserialize(ByteBufferInput input) {
        int version = input.readInt();
        super.deserialize(input);
        customArgs = input.readString();
        customConfigFileName = input.readString();
        customConfigFileContent = input.readString();
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
