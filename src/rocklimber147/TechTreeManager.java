package rocklimber147;

import arc.*;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.*;

public class TechTreeManager extends Mod {
    public TechTreeManager() {

        Events.on(ClientLoadEvent.class, e ->  {
            BaseDialog dialog = new BaseDialog("Tech Tree Manager");

            int splitIndex = -1;
            Seq<TechTree.TechNode> all = TechTree.all;

            for (int i = 0; i < all.size; i++) {
                UnlockableContent content = all.get(i).content;

                if (content.localizedName.equals("Core: Bastion")) {
                    splitIndex = i;
                    break;
                }
            }

            if (splitIndex == -1) {
                Log.err("Could not find a node with name 'Core: Bastion'");
                return;
            }

            Seq<TechTree.TechNode> serpulo = new Seq<>();
            Seq<TechTree.TechNode> erekir = new Seq<>();

            for (int i = 0; i < all.size; i++) {
                if (i < splitIndex) {
                    serpulo.add(all.get(i));
                } else {
                    erekir.add(all.get(i));
                }
            }

            Table contentArea = new Table();

            Runnable firstTab = () -> populateTree(contentArea, serpulo);
            Runnable secondTab = () -> populateTree(contentArea, erekir);
            ButtonGroup<TextButton> tabs = new ButtonGroup<>();

            Table tabTable = new Table();
            tabTable.defaults().growX().pad(4);

            tabTable.button("Serpulo", Styles.togglet, firstTab).group(tabs);
            tabTable.button("Erekir", Styles.togglet, secondTab).group(tabs);

            dialog.cont.add(tabTable).growX().row();

            dialog.cont.add(contentArea).grow().row();
            firstTab.run(); // Load first tab by default

            dialog.cont.button("Close", dialog::hide).size(150f, 50f);

            Vars.ui.menufrag.addButton("Tech Tree Manager", dialog::show);

            int[] lastWidth = {Core.graphics.getWidth()};
            dialog.cont.update(() -> {
                int currentWidth = Core.graphics.getWidth();
                if (currentWidth != lastWidth[0]) {
                    lastWidth[0] = currentWidth;
                    if (tabs.getCheckedIndex() == 0) {
                        firstTab.run();
                    } else {
                        secondTab.run();
                    }
                }
            });
        });
    }

    @Override
    public void loadContent() {
        Log.info("Loading content.");
    }
    private void populateTree(Table contentArea, Seq<TechTree.TechNode> nodes) {
        contentArea.clear();

        contentArea.pane(table -> {
            table.top().left();
            table.defaults().growX().height(50f).pad(4);
            int screenWidth = Core.graphics.getWidth();
            int itemsPerRow = screenWidth > 1600 ? 4 : screenWidth > 1200 ? 3 : screenWidth > 800 ? 2 : 1;

            int itemCount = 0;

            for (TechTree.TechNode node : nodes) {
                UnlockableContent content = node.content;

                TextButton rowButton = new TextButton("", Styles.defaultt);

                rowButton.table(t -> {
                    t.left();
                    t.image(content.uiIcon).size(40).padRight(8);
                    t.label(() -> content.localizedName).left().growX();
                }).growX();

                rowButton.update(() -> {
                    if (content.alwaysUnlocked) {
                        rowButton.setColor(1f, 1f, 1f, 0.9f);
                    } else if (content.unlocked()) {
                        rowButton.setColor(1f, 1f, 1f, 0.9f);
                    } else {
                        rowButton.setColor(1f, 1f, 1f, 0.3f);
                    }
                });

                rowButton.clicked(() -> {
                    if (content.unlocked()) {
                        content.clearUnlock();
                        node.reset();
                    } else {
                        content.quietUnlock();
                    }
                });

                table.add(rowButton);

                itemCount++;
                if (itemCount % itemsPerRow == 0) {
                    table.row();
                }
            }

        }).grow();
    }
}