package website.eccentric.tome;

import java.util.List;
import java.util.ServiceLoader;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import website.eccentric.tome.services.Dispatch;
import website.eccentric.tome.services.ModName;
import website.eccentric.tome.services.Services;
import website.eccentric.tome.services.Tome;

public class TomeItem extends Item {
    
    public TomeItem() {
        super(new Properties().stacksTo(1).tab(CreativeModeTab.TAB_TOOLS));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var player = context.getPlayer();
        var hand = context.getHand();
        var level = context.getLevel();
        var position = context.getClickedPos();
        var tome = player.getItemInHand(hand);
        var mod = Services.load(ModName.class).from(level.getBlockState(position));
        var modsBooks = Tag.getModsBooks(tome);

        if (!player.isShiftKeyDown() || !modsBooks.containsKey(mod)) return InteractionResult.PASS;

        var books = modsBooks.get(mod);
        var book = books.get(books.size() - 1);
        
        player.setItemInHand(hand, Services.load(Tome.class).convert(tome, book));

        return InteractionResult.SUCCESS;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var tome = player.getItemInHand(hand);

        if (level.isClientSide) Services.load(Dispatch.class).openTome(tome);

        return InteractionResultHolder.sidedSuccess(tome, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack tome, Level level, List<Component> tooltip, TooltipFlag advanced) {
        var modName = ServiceLoader.load(ModName.class).findFirst();
        if (!modName.isPresent()) return;

        var modsBooks = Tag.getModsBooks(tome);
        
        for (var mod : modsBooks.keySet()) {
            tooltip.add(Component.literal(modName.get().name(mod)));
            var books = modsBooks.get(mod);
            for (var book : books) {
                if (book.is(Items.AIR)) continue;
                var name = book.getHoverName().getString();
                tooltip.add(Component.literal("  " + ChatFormatting.GRAY + name));
            }
        }
    }

    public static boolean isTome(ItemStack stack) {
        if (stack.isEmpty()) return false;
        else if (stack.getItem() instanceof TomeItem) return true;
        else return Tag.isTome(stack);
    }

    public static InteractionHand inHand(Player player) {
        var hand = InteractionHand.MAIN_HAND;
        var stack = player.getItemInHand(hand);
        if (isTome(stack)) return hand;
        
        hand = InteractionHand.OFF_HAND;
        stack = player.getItemInHand(hand);
        if (isTome(stack)) return hand;

        return null;
    }
}
