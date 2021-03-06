package link.thingscloud.freeswitch.xml.domain.dialplan;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author th158
 */
@Data
@JacksonXmlRootElement(localName = "context")
public class Context implements Serializable {

    @JacksonXmlProperty(localName = "name", isAttribute = true)
    private String name;

    @JacksonXmlElementWrapper(localName = "extension", useWrapping = false)
    private List<Extension> extension;
}
