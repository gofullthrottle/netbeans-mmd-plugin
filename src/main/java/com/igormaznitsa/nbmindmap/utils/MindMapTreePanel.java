/*
 * Copyright 2015 Igor Maznitsa.
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
package com.igormaznitsa.nbmindmap.utils;

import com.igormaznitsa.nbmindmap.model.MindMap;
import com.igormaznitsa.nbmindmap.model.Topic;
import com.igormaznitsa.nbmindmap.nb.MindMapTreeCellRenderer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class MindMapTreePanel extends javax.swing.JPanel {

  private static final long serialVersionUID = 2652308291444091807L;

  private final MindMapTreeCellRenderer cellRenderer = new MindMapTreeCellRenderer();

  public MindMapTreePanel(final MindMap map, final String selectedTopicUid, final ActionListener listener) {
    initComponents();
    this.treeMindMap.setCellRenderer(this.cellRenderer);
    this.treeMindMap.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    if (map != null) {
      this.treeMindMap.setModel(map);
      if (selectedTopicUid != null) {
        final Topic topic = map.findTopicForUID(selectedTopicUid);
        if (topic != null) {
          this.treeMindMap.setSelectionPath(new TreePath(topic.getPath()));
        }
      }
    }

    this.treeMindMap.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(final MouseEvent e) {
        if (!e.isPopupTrigger() && e.getClickCount() > 1) {
          listener.actionPerformed(new ActionEvent(this, 0, "doubleClick"));
        }
      }
    });

  }

  public JTree getTree() {
    return this.treeMindMap;
  }

  public Topic getSelectedTopic() {
    final TreePath selected = this.treeMindMap.getSelectionPath();
    return selected == null ? null : (Topic) selected.getLastPathComponent();
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    treeScrollPane = new javax.swing.JScrollPane();
    treeMindMap = new javax.swing.JTree();

    setLayout(new java.awt.BorderLayout());

    treeScrollPane.setViewportView(treeMindMap);

    add(treeScrollPane, java.awt.BorderLayout.CENTER);
  }// </editor-fold>//GEN-END:initComponents


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTree treeMindMap;
  private javax.swing.JScrollPane treeScrollPane;
  // End of variables declaration//GEN-END:variables
}