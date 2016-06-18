/*
 * Copyright 2016 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.igormaznitsa.sciareto.ui.tabs;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import com.igormaznitsa.meta.annotation.MustNotContainNull;
import com.igormaznitsa.sciareto.Context;
import com.igormaznitsa.sciareto.ui.DialogProviderManager;

public class MainTabPane extends JTabbedPane implements Iterable<TabTitle> {

  private static final long serialVersionUID = -8971773653667281550L;

  private final Context context;
  
  public MainTabPane(@Nonnull final Context context) {
    super(JTabbedPane.TOP);
    this.context = context;
    this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    this.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        processPopup(e);
      }

      @Override
      public void mousePressed(MouseEvent e) {
        processPopup(e);
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        processPopup(e);
      }

      private void processPopup(@Nonnull final MouseEvent e) {
        if (e.isPopupTrigger()) {
          final JPopupMenu menu = makePopupMenu();
          if (menu != null) {
            menu.show(e.getComponent(), e.getX(), e.getY());
            e.consume();
          }
        }
      }
    });
  }

  @Nullable
  public TabTitle getCurrentTitle(){
    final int index = this.getSelectedIndex();
    return index<0 ? null : (TabTitle)this.getTabComponentAt(index);
  }

  @Nullable
  public Component getCurrentComponent() {
    final int index = this.getSelectedIndex();
    if (index<0) {
      return null;
    } else {
      return this.getComponentAt(index);
    }
  }
  
  @Nonnull
  private JPopupMenu makePopupMenu() {
    final MainTabPane theInstance = this;
    final int selected = this.getSelectedIndex();
    JPopupMenu result = null;
    if (selected >= 0) {
      final TabTitle title = (TabTitle) this.getTabComponentAt(selected);
      result = new JPopupMenu();
      
      if (title.isChanged()){
        final JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(@Nonnull final ActionEvent e) {
            title.save();
          }
        });
        result.add(saveItem);
      }
      final JMenuItem saveAsItem = new JMenuItem("Save As..");
      saveAsItem.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          title.saveAs();
        }
      });
      result.add(saveAsItem);
      
      result.add(new JSeparator());
      
      final JMenuItem closeItem = new JMenuItem("Close");
      closeItem.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull final ActionEvent e) {
          title.doSafeClose();
        }
      });
      result.add(closeItem);

      final JMenuItem closeOthers = new JMenuItem("Close Others");
      closeOthers.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull ActionEvent e) {
          final List<TabTitle> list = new ArrayList<>();
          for (final TabTitle t : theInstance) {
            if (title!=t) list.add(t);
          }
          safeCloseTabs(list.toArray(new TabTitle[list.size()]));
        }
      });
      result.add(closeOthers);
      
      final JMenuItem closeAll = new JMenuItem("Close All");
      closeAll.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(@Nonnull ActionEvent e) {
          final List<TabTitle> list = new ArrayList<>();
          for(final TabTitle t : theInstance){
            list.add(t);
          }
          safeCloseTabs(list.toArray(new TabTitle[list.size()]));
        }
      });
      result.add(closeAll);
      
      result.add(new JSeparator());
      
      final JMenuItem showInTree = new JMenuItem("Focus in the tree");
      showInTree.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          context.focusInTree(title);
        }
      });
      result.add(showInTree);
    }
    return result;
  }

  private void safeCloseTabs(@Nonnull @MustNotContainNull final TabTitle ... titles){
    boolean foundUnsaved = false;
    for(final TabTitle t : titles){
      foundUnsaved  |= t.isChanged();
    }
    if (!foundUnsaved || DialogProviderManager.getInstance().getDialogProvider().msgConfirmOkCancel("Detected unsaved","Detected unsaved documents! Close anyway?")){
      this.context.closeTab(titles);
    }
  }
  
  public void createTab(@Nonnull final TabProvider panel) {
    super.addTab("...", panel.getMainComponent());
    final int count = this.getTabCount() - 1;
    this.setTabComponentAt(count, panel.getTabTitle());
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        panel.getMainComponent().requestFocus();
      }
    });
    this.setSelectedIndex(count);
  }

  public boolean focusToFile(@Nonnull final File file) {
    for (int i = 0; i < this.getTabCount(); i++) {
      final TabTitle title = (TabTitle) this.getTabComponentAt(i);
      if (file.equals(title.getAssociatedFile())) {
        this.setSelectedIndex(i);
        return true;
      }
    }
    return false;
  }

  public boolean removeTab(@Nonnull final TabTitle title) {
    int index = -1;
    for (int i = 0; i < this.getTabCount(); i++) {
      if (this.getTabComponentAt(i) == title) {
        index = i;
        break;
      }
    }
    if (index >= 0) {
      this.removeTabAt(index);
      return true;
    }
    return false;
  }

  @Override
  @Nonnull
  public Iterator<TabTitle> iterator() {
    final List<TabTitle> result = new ArrayList<>();
    for (int i = 0; i < this.getTabCount(); i++) {
      result.add((TabTitle) this.getTabComponentAt(i));
    }
    return result.iterator();
  }

  private void clickToClose(@Nonnull final TabProvider provider) {
    int index = -1;
    for (int i = 0; i < this.getTabCount(); i++) {
      if (this.getTabComponentAt(i) == provider.getMainComponent()) {
        index = i;
        break;
      }
    }

    if (index >= 0) {
      this.removeTabAt(index);
    }
  }

}