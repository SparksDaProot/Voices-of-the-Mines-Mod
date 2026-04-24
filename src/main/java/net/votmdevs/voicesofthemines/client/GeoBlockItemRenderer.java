package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.item.GeoBlockItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class GeoBlockItemRenderer extends GeoItemRenderer<GeoBlockItem> {
    public GeoBlockItemRenderer() {
        super(new GeoBlockItemModel());
    }
}