package com.magneticcells.gui;

import com.magneticcells.MagneticCellsMod;
import com.magneticcells.items.CellData;
import com.magneticcells.items.MagneticStorageCellItem;
import com.magneticcells.network.PacketExtractItem;
import com.magneticcells.network.PacketHandler;
import com.magneticcells.network.PacketInsertItem;
import com.magneticcells.network.PacketSetFilterSlot;
import com.magneticcells.network.PacketToggleFilterMode;
import com.magneticcells.network.PacketToggleMagnet;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MagneticCellScreen extends AbstractContainerScreen<MagneticCellMenu> {

    private static final ResourceLocation TEXTURE =
        new ResourceLocation(MagneticCellsMod.MODID, "textures/gui/magnetic_cell.png");

    private static final int VISIBLE_ROWS = 3;
    private int scrollOffset = 0;
    private int totalRows    = 1;

    // ── Константы компоновки ──
    private static final int GUI_W    = MagneticCellMenu.GUI_W;  // 176
    private static final int GUI_H    = MagneticCellMenu.GUI_H;  // 202

    // Кнопка WL/BL: x=4, y=5, w=58, h=12
    private static final int BTN_X = 4,  BTN_Y = 5, BTN_W = 58, BTN_H = 12;

    // Слоты фильтра: x=4, y=20, 9 слотов по 18px → x=4..166
    private static final int FILTER_X = MagneticCellMenu.FILTER_X; // 4
    private static final int FILTER_Y = MagneticCellMenu.FILTER_Y; // 20

    // Кнопка магнита: маленький квадрат 8x18 в правом углу строки фильтра
    // x = FILTER_X + 9*18 = 4+162 = 166, y = FILTER_Y = 20, w=8, h=18
    private static final int MAGBTN_X = FILTER_X + MagneticCellMenu.FILTER_SLOTS * 18; // 166
    private static final int MAGBTN_Y = FILTER_Y;   // 20
    private static final int MAGBTN_W = 8;
    private static final int MAGBTN_H = 18;

    // Контент
    private static final int CONTENT_X  = MagneticCellMenu.CONTENT_X; // 4
    private static final int CONTENT_Y  = MagneticCellMenu.CONTENT_Y; // 56
    private static final int SCROLL_X   = CONTENT_X + MagneticCellMenu.CONTENT_COLS * 18 + 2; // 166

    // Инвентарь — INV_X=4, совпадает с FILTER_X чтобы всё выровнено
    private static final int INV_X    = MagneticCellMenu.INV_X;    // 4
    private static final int INV_Y    = MagneticCellMenu.INV_Y;    // 122
    private static final int HOTBAR_Y = MagneticCellMenu.HOTBAR_Y; // 180
    private static final int INV_LABEL_Y = 114;

    // Цвета слотов фильтра
    private static final int SLOT_WL_BG = 0xFF3A5A3A;
    private static final int SLOT_WL_L  = 0xFF5A7A5A;
    private static final int SLOT_WL_D  = 0xFF1A3A1A;
    private static final int SLOT_WL_IN = 0xFF2A4A2A;
    private static final int SLOT_BL_BG = 0xFF5A3A3A;
    private static final int SLOT_BL_L  = 0xFF7A5A5A;
    private static final int SLOT_BL_D  = 0xFF3A1A1A;
    private static final int SLOT_BL_IN = 0xFF4A2A2A;

    public MagneticCellScreen(MagneticCellMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth  = GUI_W;
        this.imageHeight = GUI_H;
    }

    @Override protected void init() { super.init(); }

    @Override
    protected void renderBg(GuiGraphics g, float pt, int mx, int my) {
        int x = leftPos, y = topPos;

        // Основной фон
        g.fill(x, y, x+GUI_W, y+GUI_H, 0xFFC6C6C6);
        drawBorder(g, x, y, GUI_W, GUI_H);

        // ── Панель фильтра (включает кнопку магнита справа) ──
        // Панель чуть шире чтобы вместить кнопку магнита: до x=174
        drawPanel(g, x+2, y+2, GUI_W-4, 36);

        // Кнопка WL/BL
        boolean wl = menu.getFilterMode() == CellData.MODE_WHITELIST;
        drawBtn(g, x+BTN_X, y+BTN_Y, BTN_W, BTN_H,
            wl ? 0xFF2A6A2A : 0xFF6A2A2A,
            wl ? 0xFF4A9A4A : 0xFF9A4A4A,
            wl ? 0xFF0A4A0A : 0xFF4A0A0A);
        String modeStr = wl ? "\u2666 Whitelist" : "\u2666 Blacklist";
        g.drawString(font, modeStr,
            x+BTN_X+(BTN_W-font.width(modeStr))/2, y+BTN_Y+2,
            wl ? 0x88FF88 : 0xFF8888, true);

        // Слоты фильтра (9 штук, зелёные/красные)
        int slBg = wl ? SLOT_WL_BG : SLOT_BL_BG;
        int slL  = wl ? SLOT_WL_L  : SLOT_BL_L;
        int slD  = wl ? SLOT_WL_D  : SLOT_BL_D;
        int slIn = wl ? SLOT_WL_IN : SLOT_BL_IN;
        for (int i = 0; i < MagneticCellMenu.FILTER_SLOTS; i++)
            drawColorSlot(g, x+FILTER_X+i*18, y+FILTER_Y, slBg, slL, slD, slIn);

        // Предметы в слотах фильтра
        for (int i = 0; i < MagneticCellMenu.FILTER_SLOTS; i++) {
            ItemStack fs = menu.getFilterSlot(i);
            if (!fs.isEmpty()) g.renderItem(fs, x+FILTER_X+i*18+1, y+FILTER_Y+1);
        }

        // Кнопка магнита — маленький вертикальный прямоугольник 8x18 справа от слотов
        ItemStack cell = menu.getCellStack();
        boolean magnetOn = cell.isEmpty() || CellData.getMagnetEnabled(cell);
        // Фон кнопки
        int magMid   = magnetOn ? 0xFF1A5A6A : 0xFF444444;
        int magLight = magnetOn ? 0xFF3A8A9A : 0xFF666666;
        int magDark  = magnetOn ? 0xFF0A3A4A : 0xFF222222;
        g.fill(x+MAGBTN_X,   y+MAGBTN_Y,   x+MAGBTN_X+MAGBTN_W, y+MAGBTN_Y+MAGBTN_H, magMid);
        g.fill(x+MAGBTN_X,   y+MAGBTN_Y,   x+MAGBTN_X+MAGBTN_W, y+MAGBTN_Y+1,         magLight);
        g.fill(x+MAGBTN_X,   y+MAGBTN_Y,   x+MAGBTN_X+1,         y+MAGBTN_Y+MAGBTN_H, magLight);
        g.fill(x+MAGBTN_X,   y+MAGBTN_Y+MAGBTN_H-1, x+MAGBTN_X+MAGBTN_W, y+MAGBTN_Y+MAGBTN_H, magDark);
        g.fill(x+MAGBTN_X+MAGBTN_W-1, y+MAGBTN_Y, x+MAGBTN_X+MAGBTN_W, y+MAGBTN_Y+MAGBTN_H, magDark);
        // Иконка: ◎ (ON) или ○ (OFF) — вертикально по центру
        String magIcon = magnetOn ? "\u25CE" : "\u25CB";
        int iconX = x+MAGBTN_X+(MAGBTN_W-font.width(magIcon))/2;
        int iconY = y+MAGBTN_Y+(MAGBTN_H-8)/2;
        g.drawString(font, magIcon, iconX, iconY, magnetOn ? 0x88EEFF : 0xAAAAAA, true);

        drawDiv(g, x+2, y+40, GUI_W-4);

        // ── Панель контента ──
        drawPanel(g, x+2, y+44, GUI_W-4, 10+VISIBLE_ROWS*18+2);

        // Статистика
        if (!cell.isEmpty() && cell.getItem() instanceof MagneticStorageCellItem ci) {
            long stored = CellData.getTotalStored(cell);
            int  types  = CellData.getStored(cell).size();
            String stat = stored+" / "+ci.getMaxItems()+"  |  "+types+" / "+ci.getMaxTypes()+" \u0442\u0438\u043f\u043e\u0432";
            g.drawString(font, stat, x+CONTENT_X+2, y+47, 0x707070, false);
        }

        // Слоты контента
        for (int row = 0; row < VISIBLE_ROWS; row++)
            for (int col = 0; col < MagneticCellMenu.CONTENT_COLS; col++)
                drawSlot(g, x+CONTENT_X+col*18, y+CONTENT_Y+row*18);

        // Скроллбар
        int sbH = VISIBLE_ROWS * 18;
        g.fill(x+SCROLL_X, y+CONTENT_Y, x+SCROLL_X+6, y+CONTENT_Y+sbH, 0xFF3A3A3A);
        if (totalRows > VISIBLE_ROWS) {
            int tH = Math.max(6, sbH * VISIBLE_ROWS / totalRows);
            int tY = y+CONTENT_Y+(sbH-tH)*scrollOffset/Math.max(1, totalRows-VISIBLE_ROWS);
            g.fill(x+SCROLL_X+1, tY, x+SCROLL_X+5, tY+tH, 0xFFAAAAAA);
        }

        renderContentItems(g);

        drawDiv(g, x+2, y+CONTENT_Y+VISIBLE_ROWS*18+2, GUI_W-4);

        // ── Инвентарь ──
        g.drawString(font, "\u0418\u043d\u0432\u0435\u043d\u0442\u0430\u0440\u044c", x+INV_X, y+INV_LABEL_Y, 0x404040, false);

        // Слоты инвентаря — рисуем фон в тех же координатах что vanilla рендерит предметы
        // slot.x = INV_X + col*18, предмет рисуется на leftPos+slot.x+1
        // наш drawSlot рисует внутренний квадрат с x+1 — совпадает
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                drawSlot(g, x + INV_X + col * 18, y + INV_Y + row * 18);

        drawDiv(g, x+2, y+HOTBAR_Y-4, GUI_W-4);

        for (int col = 0; col < 9; col++)
            drawSlot(g, x + INV_X + col * 18, y + HOTBAR_Y);
    }

    private void renderContentItems(GuiGraphics g) {
        ItemStack cell = menu.getCellStack();
        if (cell.isEmpty()) return;
        List<CellData.StoredStack> stored = CellData.getStored(cell);
        totalRows = Math.max(VISIBLE_ROWS,
            (int)Math.ceil(stored.size()/(double)MagneticCellMenu.CONTENT_COLS));
        scrollOffset = Math.max(0, Math.min(scrollOffset, totalRows-VISIBLE_ROWS));

        int start = scrollOffset * MagneticCellMenu.CONTENT_COLS;
        for (int row = 0; row < VISIBLE_ROWS; row++) {
            for (int col = 0; col < MagneticCellMenu.CONTENT_COLS; col++) {
                int idx = start + row*MagneticCellMenu.CONTENT_COLS + col;
                if (idx >= stored.size()) continue;
                CellData.StoredStack ss = stored.get(idx);
                int ix = leftPos+CONTENT_X+col*18;
                int iy = topPos +CONTENT_Y+row*18;
                g.renderItem(ss.type, ix, iy);

                // Цифры 0.75 scale, правый нижний угол
                String amt = fmtAmt(ss.amount);
                float scale = 0.75f;
                float tx = ix + 17 - font.width(amt) * scale;
                float ty = iy + 17 - 7 * scale;
                g.pose().pushPose();
                g.pose().translate(tx, ty, 250);
                g.pose().scale(scale, scale, 1f);
                g.drawString(font, amt, 0, 0, 0xFFFFFF, true);
                g.pose().popPose();
            }
        }
    }

    private static String fmtNum(long n) {
        if (n >= 1_000_000_000L) return String.format("%.0fG", n / 1_000_000_000.0);
        if (n >= 1_000_000L)     return String.format("%.0fM", n / 1_000_000.0);
        if (n >= 1_000L)         return String.format("%.0fK", n / 1_000.0);
        return String.valueOf(n);
    }

    private String fmtAmt(long n) {
        if (n >= 1_000_000_000) return (n/1_000_000_000)+"G";
        if (n >= 1_000_000)     return (n/1_000_000)+"M";
        if (n >= 1_000)         return (n/1_000)+"k";
        return String.valueOf(n);
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mx, int my) {
        // Заголовок между кнопками
        String t = title.getString();
        g.drawString(font, t, BTN_X+BTN_W+4, BTN_Y+2, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g);
        super.render(g, mx, my, pt);
        renderContentTooltip(g, mx, my);
        renderMagnetTooltip(g, mx, my);
        renderTooltip(g, mx, my);
    }

    private void renderContentTooltip(GuiGraphics g, int mx, int my) {
        int idx = hoveredContentIdx(mx, my);
        if (idx < 0) return;
        ItemStack cell = menu.getCellStack();
        if (cell.isEmpty()) return;
        List<CellData.StoredStack> stored = CellData.getStored(cell);
        if (idx >= stored.size()) return;
        CellData.StoredStack ss = stored.get(idx);
        List<Component> lines = new ArrayList<>();
        lines.add(ss.type.getDisplayName());
        lines.add(Component.literal("\u00a77\u041a\u043e\u043b-\u0432\u043e: \u00a7f" + ss.amount));
        lines.add(Component.literal("\u00a77\u041b\u041a\u041c \u00a7f- 1\u0448\u0442   \u00a77Shift+\u041b\u041a\u041c \u00a7f- \u0432\u0441\u0451"));
        g.renderComponentTooltip(font, lines, mx-leftPos, my-topPos);
    }

    private void renderMagnetTooltip(GuiGraphics g, int mx, int my) {
        if (!isOver(mx, my, MAGBTN_X, MAGBTN_Y, MAGBTN_W, MAGBTN_H)) return;
        ItemStack cell = menu.getCellStack();
        boolean on = cell.isEmpty() || CellData.getMagnetEnabled(cell);
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal(on ? "\u00a7a\u041c\u0430\u0433\u043d\u0438\u0442: \u0432\u043a\u043b\u044e\u0447\u0435\u043d" : "\u00a7c\u041c\u0430\u0433\u043d\u0438\u0442: \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d"));
        lines.add(Component.literal("\u00a77\u041a\u043b\u0438\u043a \u0447\u0442\u043e\u0431\u044b \u043f\u0435\u0440\u0435\u043a\u043b\u044e\u0447\u0438\u0442\u044c"));
        g.renderComponentTooltip(font, lines, mx-leftPos, my-topPos);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        // Кнопка WL/BL
        if (isOver(mx, my, BTN_X, BTN_Y, BTN_W, BTN_H)) {
            PacketHandler.sendToServer(new PacketToggleFilterMode());
            menu.toggleFilterMode();
            return true;
        }
        // Кнопка магнита
        if (isOver(mx, my, MAGBTN_X, MAGBTN_Y, MAGBTN_W, MAGBTN_H)) {
            PacketHandler.sendToServer(new PacketToggleMagnet());
            ItemStack cell = menu.getCellStack();
            if (!cell.isEmpty()) CellData.toggleMagnet(cell);
            return true;
        }
        // Слоты фильтра
        int fi = hoveredFilter(mx, my);
        if (fi >= 0) {
            ItemStack carried = minecraft.player.containerMenu.getCarried();
            ItemStack toSet = (btn == 1 || carried.isEmpty()) ? ItemStack.EMPTY : carried.copyWithCount(1);
            PacketHandler.sendToServer(new PacketSetFilterSlot(fi, toSet));
            menu.setFilterSlot(fi, toSet);
            return true;
        }
        // Контент
        if (btn == 0) {
            ItemStack carried = minecraft.player.containerMenu.getCarried();
            int ci = hoveredContentIdx(mx, my);
            if (ci >= 0 && carried.isEmpty()) {
                PacketHandler.sendToServer(new PacketExtractItem(ci, hasShiftDown()));
                return true;
            }
            if (!carried.isEmpty() && isOverContent(mx, my)) {
                PacketHandler.sendToServer(new PacketInsertItem(-1));
                return true;
            }
        }
        // Инвентарь — shift+клик вставляет в ячейку, обычный клик — vanilla
        if (btn == 0 && hasShiftDown()) {
            int invSlot = hoveredInvSlot(mx, my);
            if (invSlot >= 0) {
                PacketHandler.sendToServer(new PacketInsertItem(invSlot));
                return true;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (isOverContent(mx, my)) {
            int max = Math.max(0, totalRows-VISIBLE_ROWS);
            scrollOffset = (int)Math.max(0, Math.min(max, scrollOffset-delta));
            return true;
        }
        return super.mouseScrolled(mx, my, delta);
    }

    // ── Хелперы координат ──
    private boolean isOver(double mx, double my, int rx, int ry, int w, int h) {
        int px = (int)(mx-leftPos), py = (int)(my-topPos);
        return px >= rx && px < rx+w && py >= ry && py < ry+h;
    }
    private boolean isOverContent(double mx, double my) {
        return isOver(mx, my, CONTENT_X, CONTENT_Y, MagneticCellMenu.CONTENT_COLS*18, VISIBLE_ROWS*18);
    }
    private int hoveredFilter(double mx, double my) {
        for (int i = 0; i < MagneticCellMenu.FILTER_SLOTS; i++)
            if (isOver(mx, my, FILTER_X+i*18, FILTER_Y, 18, 18)) return i;
        return -1;
    }
    private int hoveredContentIdx(double mx, double my) {
        for (int row = 0; row < VISIBLE_ROWS; row++)
            for (int col = 0; col < MagneticCellMenu.CONTENT_COLS; col++)
                if (isOver(mx, my, CONTENT_X+col*18, CONTENT_Y+row*18, 18, 18)) {
                    ItemStack cell = menu.getCellStack();
                    if (cell.isEmpty()) return -1;
                    int dataIdx = (scrollOffset+row)*MagneticCellMenu.CONTENT_COLS+col;
                    return dataIdx < CellData.getStored(cell).size() ? dataIdx : -1;
                }
        return -1;
    }
    private int hoveredInvSlot(double mx, double my) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                if (isOver(mx, my, INV_X+col*18, INV_Y+row*18, 18, 18))
                    return col+row*9+9;
        for (int col = 0; col < 9; col++)
            if (isOver(mx, my, INV_X+col*18, HOTBAR_Y, 18, 18)) return col;
        return -1;
    }

    // ── Рисование ──
    private void drawBorder(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x,     y,     x+w,   y+1,   0xFF373737);
        g.fill(x,     y+h-1, x+w,   y+h,   0xFFFFFFFF);
        g.fill(x,     y,     x+1,   y+h,   0xFF373737);
        g.fill(x+w-1, y,     x+w,   y+h,   0xFFFFFFFF);
    }
    private void drawPanel(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x,     y,     x+w, y+h, 0xFFBBBBBB);
        g.fill(x,     y,     x+w, y+1, 0xFF555555);
        g.fill(x,     y,     x+1, y+h, 0xFF555555);
        g.fill(x,     y+h-1, x+w, y+h, 0xFFEEEEEE);
        g.fill(x+w-1, y,     x+w, y+h, 0xFFEEEEEE);
    }
    private void drawSlot(GuiGraphics g, int x, int y) {
        // Тёмный бордер снаружи (x-1, y-1) — как в vanilla
        g.fill(x-1,  y-1,  x+17, y,    0xFF373737); // верх
        g.fill(x-1,  y-1,  x,    y+17, 0xFF373737); // лево
        g.fill(x-1,  y+17, x+18, y+18, 0xFFFFFFFF); // низ
        g.fill(x+17, y-1,  x+18, y+18, 0xFFFFFFFF); // право
        g.fill(x,    y,    x+16, y+16, 0xFF6B6B6B);  // внутренняя область
    }
    private void drawColorSlot(GuiGraphics g, int x, int y, int bg, int light, int dark, int inner) {
        g.fill(x,    y,    x+18, y+18, bg);
        g.fill(x,    y,    x+18, y+1,  dark);
        g.fill(x,    y,    x+1,  y+18, dark);
        g.fill(x,    y+17, x+18, y+18, light);
        g.fill(x+17, y,    x+18, y+18, light);
        g.fill(x+1,  y+1,  x+17, y+17, inner);
    }
    private void drawBtn(GuiGraphics g, int x, int y, int w, int h, int mid, int light, int dark) {
        g.fill(x,     y,     x+w,   y+h,   mid);
        g.fill(x,     y,     x+w,   y+1,   light);
        g.fill(x,     y,     x+1,   y+h,   light);
        g.fill(x,     y+h-1, x+w,   y+h,   dark);
        g.fill(x+w-1, y,     x+w,   y+h,   dark);
    }
    private void drawDiv(GuiGraphics g, int x, int y, int w) {
        g.fill(x, y,   x+w, y+1, 0xFF555555);
        g.fill(x, y+2, x+w, y+3, 0xFFEEEEEE);
    }
}
