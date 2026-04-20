package net.votmdevs.voicesofthemines.client;

import net.votmdevs.voicesofthemines.VoicesOfTheMines;
import net.votmdevs.voicesofthemines.entity.HookEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class HookRenderer extends EntityRenderer<HookEntity> {
    private final ItemRenderer itemRenderer;

    public HookRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(HookEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));

        ItemStack stack = new ItemStack(VoicesOfTheMines.HOOK_PART.get());
        this.itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());

        poseStack.popPose();

        Player player = entity.level().getPlayerByUUID(entity.getOwnerUUID());
        if (player != null) {
            poseStack.pushPose();

            int handOffset = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
            ItemStack itemInHand = player.getMainHandItem();
            if (itemInHand.getItem() != VoicesOfTheMines.HOOK_ITEM.get()) {
                handOffset = -handOffset;
            }

            float anim = player.getAttackAnim(partialTicks);
            float sinAnim = Mth.sin(Mth.sqrt(anim) * (float)Math.PI);
            float bodyRot = Mth.lerp(partialTicks, player.yBodyRotO, player.yBodyRot) * ((float)Math.PI / 180F);
            double sinBody = Mth.sin(bodyRot);
            double cosBody = Mth.cos(bodyRot);

            Vec3 playerHandPos;

            if (this.entityRenderDispatcher.options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player) {
                double fov = this.entityRenderDispatcher.options.fov().get() / 100.0D;
                Vec3 fovOffset = new Vec3((double)handOffset * -0.36D * fov, -0.045D * fov, 0.4D);

                fovOffset = fovOffset.xRot(-Mth.lerp(partialTicks, player.xRotO, player.getXRot()) * ((float)Math.PI / 180F));
                fovOffset = fovOffset.yRot(-Mth.lerp(partialTicks, player.yRotO, player.getYRot()) * ((float)Math.PI / 180F));
                fovOffset = fovOffset.yRot(sinAnim * 0.5F);
                fovOffset = fovOffset.xRot(-sinAnim * 0.7F);

                playerHandPos = player.getEyePosition(partialTicks).add(fovOffset);
            } else {
                double handX = (double)handOffset * 0.35D;
                double handY = 0.8D;
                double x = player.getX() - sinBody * handX - cosBody * 0.8D;
                double y = player.getY() + handY;
                double z = player.getZ() - cosBody * handX + sinBody * 0.8D;
                playerHandPos = new Vec3(x, y, z);
            }

            double hookX = Mth.lerp((double)partialTicks, entity.xo, entity.getX());
            double hookY = Mth.lerp((double)partialTicks, entity.yo, entity.getY()) + 0.1D; // Центр крюка
            double hookZ = Mth.lerp((double)partialTicks, entity.zo, entity.getZ());

            float dx = (float)(playerHandPos.x - hookX);
            float dy = (float)(playerHandPos.y - hookY);
            float dz = (float)(playerHandPos.z - hookZ);

            VertexConsumer consumer = buffer.getBuffer(RenderType.lines());
            Matrix4f matrix4f = poseStack.last().pose();
            Matrix3f normalMatrix = poseStack.last().normal();

            int r = 20, g = 20, b = 20, a = 255;

            float t = 0.002F;
            float d = t * 0.7F;

            consumer.vertex(matrix4f, 0.0F, 0.0F, 0.0F).color(r, g, b, a).normal(normalMatrix, dx, dy, dz).endVertex();
            consumer.vertex(matrix4f, dx, dy, dz).color(r, g, b, a).normal(normalMatrix, dx, dy, dz).endVertex();

            consumer.vertex(matrix4f, 0.0F, t, 0.0F).color(r, g, b, a).normal(normalMatrix, dx, dy, dz).endVertex();
            consumer.vertex(matrix4f, dx, dy + t, dz).color(r, g, b, a).normal(normalMatrix, dx, dy, dz).endVertex();

            consumer.vertex(matrix4f, 0.0F, -t, 0.0F).color(r, g, b, a).normal(normalMatrix, dx, dy, dz).endVertex();
            consumer.vertex(matrix4f, dx, dy - t, dz).color(r, g, b, a).normal(normalMatrix, dx, dy, dz).endVertex();

            consumer.vertex(matrix4f, t, 0.0F, 0.0F).color(r, g, b, a).normal(normalMatrix, dx, dy, dz).endVertex();
            consumer.vertex(matrix4f, dx + t, dy, dz).color(r, g, b, a).normal(normalMatrix, dx, dy, dz).endVertex();

            consumer.vertex(matrix4f, -t, 0.0F, 0.0F).color(r, g, b, a).normal(normalMatrix, dx, dy, dz).endVertex();
            consumer.vertex(matrix4f, dx - t, dy, dz).color(r, g, b, a).normal(normalMatrix, dx, dy, dz).endVertex();

            consumer.vertex(matrix4f, d, d, 0.0F).color(r, g, b, a).normal(normalMatrix, dx, dy, dz).endVertex();
            consumer.vertex(matrix4f, dx + d, dy + d, dz).color(r, g, b, a).normal(normalMatrix, dx, dy, dz).endVertex();

            consumer.vertex(matrix4f, -d, d, 0.0F).color(r, g, b, a).normal(normalMatrix, dx, dy, dz).endVertex();
            consumer.vertex(matrix4f, dx - d, dy + d, dz).color(r, g, b, a).normal(normalMatrix, dx, dy, dz).endVertex();

            consumer.vertex(matrix4f, d, -d, 0.0F).color(r, g, b, a).normal(normalMatrix, dx, dy, dz).endVertex();
            consumer.vertex(matrix4f, dx + d, dy - d, dz).color(r, g, b, a).normal(normalMatrix, dx, dy, dz).endVertex();

            consumer.vertex(matrix4f, -d, -d, 0.0F).color(r, g, b, a).normal(normalMatrix, dx, dy, dz).endVertex();
            consumer.vertex(matrix4f, dx - d, dy - d, dz).color(r, g, b, a).normal(normalMatrix, dx, dy, dz).endVertex();

            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(HookEntity entity) {
        return net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS;
    }
}