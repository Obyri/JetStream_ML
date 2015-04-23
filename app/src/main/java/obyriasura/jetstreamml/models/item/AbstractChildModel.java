package obyriasura.jetstreamml.models.item;

import org.fourthline.cling.model.meta.Service;

import java.net.URL;

/**
 * Created by obyri on 4/1/15.
 */
public abstract class AbstractChildModel extends AbstractItemModel {

    private AbstractItemModel parent;
    private URL iconUrl;

    public AbstractChildModel(String id, Service contentDirectoryService, AbstractItemModel parent) {
        super(id);
        setContentDirectoryService(contentDirectoryService);
        this.parent = parent;
    }

    public AbstractItemModel getParent() {
        return parent;
    }

    public void setParent(AbstractItemModel parent) {
        this.parent = parent;
    }

    protected abstract void addIconUrl();

    @Override
    public URL getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(URL iconUrl) {
        this.iconUrl = iconUrl;
    }
}
