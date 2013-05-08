//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/vilya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.stage.tools.editor;

import java.io.File;
import java.io.IOException;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.xml.sax.SAXException;

import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.util.MenuUtil;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.media.ManagedJFrame;

import com.threerings.miso.tile.BaseTileSet;

import com.threerings.whirled.data.AuxModel;
import com.threerings.whirled.tools.xml.SceneParser;

import com.threerings.stage.data.StageMisoSceneModel;
import com.threerings.stage.data.StageScene;
import com.threerings.stage.data.StageSceneModel;
import com.threerings.stage.tools.editor.util.EditorContext;
import com.threerings.stage.tools.editor.util.EditorDialogUtil;
import com.threerings.stage.tools.xml.StageSceneParser;
import com.threerings.stage.tools.xml.StageSceneWriter;

import static com.threerings.stage.Log.log;

public class EditorFrame extends ManagedJFrame
    implements EditorScenePanel.SceneModelListener
{
    public EditorFrame (StageSceneWriter writer)
    {
        _writer = writer;
        // treat a closing window as a request to quit
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing (WindowEvent e) {
                handleQuit(null);
            }
        });

        // set our initial window title
        setFilePath(null);
    }

    public void init (EditorContext ctx, String target)
    {
        _ctx = ctx;

        // create the editor data model for reference by the panels
        _model = new EditorModel(_ctx.getTileManager());

        // create the file chooser used for saving and loading scenes
        if (target == null) {
            target = EditorConfig.config.getValue("editor.last_dir",
                System.getProperty("user.dir"));
        }
        _chooser = (target == null) ? new JFileChooser() : new JFileChooser(target);
        _chooser.setFileFilter(new FileFilter() {
            @Override public boolean accept (File f) {
                return (f.isDirectory() || f.getName().endsWith(".xml"));
            }
            @Override public String getDescription () {
                return "XML Files";
            }
        });

        // instead of using a popup, we slip the file chooser into the
        // main interface (so the editor can run full-screen); thus we
        // can't use the standard business and have to add an action
        // listen to our file chooser
        _chooser.addActionListener(_openListener);

        // set up the menu bar
        createMenuBar();

        // create a top-level panel to manage everything
        JPanel top = new JPanel(new BorderLayout());

        // create a sub-panel to contain the toolbar and scene view
        _main = new JPanel(new BorderLayout());

        // set up the scene view panel with a default scene
        _svpanel = createScenePanel();
        _main.add(_svpanel, BorderLayout.CENTER);

        // create a toolbar for action selection and other options
        JPanel upper = new JPanel(new HGroupLayout(GroupLayout.NONE, GroupLayout.LEFT));
        upper.add(new EditorToolBarPanel(_ctx.getTileManager(), _model), GroupLayout.FIXED);
        _sceneInfoPanel = new SceneInfoPanel(_ctx, _model, _svpanel);
        upper.add(_sceneInfoPanel, GroupLayout.FIXED);
        _main.add(upper, BorderLayout.NORTH);

        // create a couple of scroll bars
        JScrollBar horiz = new JScrollBar(JScrollBar.HORIZONTAL);
        horiz.setBlockIncrement(500);
        horiz.setModel(_svpanel.getHorizModel());
        _main.add(horiz, BorderLayout.SOUTH);
        JScrollBar vert = new JScrollBar(JScrollBar.VERTICAL);
        vert.setBlockIncrement(500);
        vert.setModel(_svpanel.getVertModel());
        _main.add(vert, BorderLayout.EAST);

        // add the scene view and toolbar to the top-level panel
        top.add(_main, BorderLayout.CENTER);

        // set up our left sidebar panel
        JPanel west = GroupLayout.makeVStretchBox(5);
        _tpanel = new TileInfoPanel(_ctx, _model);
        west.add(_tpanel);

        JPanel boxPane = GroupLayout.makeHBox();
        boxPane.add(_scrollBox = new EditorScrollBox(_svpanel));
        west.add(boxPane, GroupLayout.FIXED);

        // add the sub-panel to the top panel
        top.add(west, BorderLayout.WEST);

        // now add our top-level panel
        getContentPane().add(top, BorderLayout.CENTER);

        // observe mouse-wheel events in the scene view panel to allow
        // scrolling the wheel to select the tile to be placed
        _svpanel.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved (MouseWheelEvent e) {
                if (e.getWheelRotation() < 0) {
                    _tpanel.selectPreviousTile();
                } else {
                    _tpanel.selectNextTile();
                }
            }
        });

        // finally set a default scene
        String lastFile = EditorConfig.config.getValue("editor.last_file", "");
        boolean loaded = false;
        if (!lastFile.equals("")) {
            try {
                loaded = loadScene(lastFile);
            } catch (Exception e) {
                log.warning("Unable to load last scene, creating new one", "file", lastFile, e);
            }
        }
        if (!loaded) {
            newScene();
        }
    }

    /**
     * Creates the EditorScenePanel to use in this frame.
     */
    protected EditorScenePanel createScenePanel ()
    {
        return new EditorScenePanel(_ctx, this, _model, this);
    }

    /**
     * Create the menu bar and menu items and add them to the frame.
     */
    public void createMenuBar ()
    {
        // create the "File" menu
        JMenu menuFile = new JMenu("File");
        createFileMenu(menuFile);

        // create the "Edit" menu
//         JMenu menuEdit = new JMenu("Edit");
//         MenuUtil.addMenuItem(menuEdit, "Undo", this, "handleUndo");
//         MenuUtil.addMenuItem(menuEdit, "Cut", this, "handleCut");
//         MenuUtil.addMenuItem(menuEdit, "Copy", this, "handleCopy");
//         MenuUtil.addMenuItem(menuEdit, "Paste", this, "handlePaste");
//         MenuUtil.addMenuItem(menuEdit, "Clear", this, "handleClear");
//         MenuUtil.addMenuItem(menuEdit, "Select All", this, "handleSelectAll");
//         menuEdit.setMnemonic(KeyEvent.VK_E);

        // create the "Actions" menu
        JMenu menuActions = new JMenu("Actions");
        createActionsMenu(menuActions);

        // create the "Settings" menu
        KeyStroke accel = null;
        JMenu menuSettings = new JMenu("Settings");
        menuSettings.setMnemonic(KeyEvent.VK_S);
        accel = KeyStroke.getKeyStroke(
            KeyEvent.VK_SEMICOLON, ActionEvent.CTRL_MASK);
        MenuUtil.addMenuItem(menuSettings, "Preferences...", KeyEvent.VK_P,
                             accel, this, "handlePreferences");

        // create the menu bar
        JMenuBar bar = new JMenuBar();
        bar.add(menuFile);
        // bar.add(menuEdit);
        bar.add(menuActions);
        bar.add(menuSettings);

        // add the menu bar to the frame
        setJMenuBar(bar);
    }

    protected void createFileMenu (JMenu menuFile)
    {
        KeyStroke accel = null;
        menuFile.setMnemonic(KeyEvent.VK_F);
        accel = KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK);
        MenuUtil.addMenuItem(menuFile, "New", KeyEvent.VK_N, accel,
                             this, "handleNew");
        accel = KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK);
        MenuUtil.addMenuItem(menuFile, "Open", KeyEvent.VK_O, accel,
                             this, "handleOpen");
        //addMenuItem(menuFile, "Close", KeyEvent.VK_C);
        accel = KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK);
        MenuUtil.addMenuItem(menuFile, "Save", KeyEvent.VK_S, accel,
                             this, "handleSave");
        MenuUtil.addMenuItem(menuFile, "Save As", KeyEvent.VK_A, null,
                             this, "handleSaveAs");
        accel = KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK);
        MenuUtil.addMenuItem(menuFile, "Quit", KeyEvent.VK_Q, accel,
                             this, "handleQuit");
    }

    protected void createActionsMenu (JMenu menuActions)
    {
        KeyStroke accel = null;
        MenuUtil.addMenuItem(menuActions, "Load (reload) test tiles", this,
            "handleTestTiles");
        menuActions.setMnemonic(KeyEvent.VK_A);

        accel = KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK);
        MenuUtil.addMenuItem(menuActions, "Make default base tile",
                             KeyEvent.VK_M, accel, this, "handleSetDefBase");

        accel = KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK);
        MenuUtil.addMenuItem(menuActions, "Update mini view",
                             KeyEvent.VK_M, accel, this, "updateMiniView");

        accel = KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK);
        MenuUtil.addMenuItem(menuActions, "Undo",
            KeyEvent.VK_Z, accel, this, "undo");

        accel = KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK);
        MenuUtil.addMenuItem(menuActions, "Redo",
            KeyEvent.VK_Y, accel, this, "redo");
    }

    protected void setScene (StageScene scene)
    {
        // save off the scene objects
        _scene = scene;

        // display the name of the scene
        _sceneInfoPanel.setScene(_scene);

        // set up the scene view panel with the new scene
        _svpanel.setScene(_scene);
        _svpanel.repaint();
    }

    protected void newScene ()
    {
        try {
            StageSceneModel model = StageSceneModel.blankStageSceneModel();
            model.type = StageSceneModel.WORLD;
            setScene(new StageScene(model, null));

        } catch (Exception e) {
            log.warning("Unable to set blank scene.", e);
        }
    }

    public void setMisoSceneModel (StageMisoSceneModel model)
    {
        AuxModel[] models = _scene.getSceneModel().auxModels;
        for (int ii = 0; ii < models.length; ii++) {
            if (models[ii] instanceof StageMisoSceneModel) {
                models[ii] = model;
            }
        }
    }

    /**
     * Creates a blank scene and configures the editor to begin editing
     * it. Eventually this should make sure the user has a chance to save
     * any scene for which editing is currently in progress.
     */
    public void handleNew (ActionEvent evt)
    {
        // clear out the fiel path so that "Save" pops up "Save as" rather
        // than overwriting whatever you were editing before
        setFilePath(null);

        newScene();
    }

    /**
     * Presents the user with an open file dialog and loads the scenes
     * from the selected file.
     */
    public void handleOpen (ActionEvent evt)
    {
        _chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        _main.remove(_svpanel);
        _main.add(_chooser, BorderLayout.CENTER);
        SwingUtil.refresh(_main);
    }

    /**
     * Loads the scene from the specified path into the editor and displays an error dialog if it
     * fails.
     */
    public void openScene (String path)
    {
        String errmsg = null;

        // attempt loading and installation of the scene
        try {
            if (!loadScene(path)) {
                errmsg = "No scene data found";
            }

        } catch (Exception e) {
            errmsg = "Parse error: " + e;
            log.warning(e);
        }

        if (errmsg != null) {
            errmsg = "Unable to load scene from " + path + ":\n" + errmsg;
            JOptionPane.showMessageDialog(
                this, errmsg, "Load error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads the scene from the specified path into the editor and returns true if it succeeds.
     */
    protected boolean loadScene (String path)
        throws IOException, SAXException
    {
        StageSceneModel model = (StageSceneModel)_parser.parseScene(path);
        if (model == null) {
            return false;
        }
        setScene(new StageScene(model, null));
        // keep track of the path for later saving
        setFilePath(path);
        return true;
    }

    /**
     * Save the scenes to the file they were last associated with.
     */
    public void handleSave (ActionEvent evt)
    {
        if (_filepath == null) {
            handleSaveAs(evt);
            return;
        }

        if (!checkSaveOk()) {
            return;
        }

        try {
            // we don't write directly to the file in question, in case
            // something craps out during the writing process
            File tmpfile = new File(_filepath + ".tmp");
            _writer.writeScene(tmpfile, _scene.getSceneModel());

            // now that we've successfully written the new file, delete
            // the old file
            File sfile = new File(_filepath);
            if (!sfile.delete()) {
                log.warning("Aiya! Not able to remove " + _filepath +
                            " so that we can replace it with " +
                            tmpfile.getPath() + ".");
            }

            // now rename the new save file into place
            if (!tmpfile.renameTo(sfile)) {
                log.warning("Fork! Not able to rename " + tmpfile.getPath() +
                            " to " + _filepath + ".");
            }

        } catch (Exception e) {
            String errmsg = "Unable to save scene to " + _filepath + ":\n" + e;
            JOptionPane.showMessageDialog(
                this, errmsg, "Save error", JOptionPane.ERROR_MESSAGE);
            log.warning("Error writing scene [fname=" + _filepath + "].", e);
        }
    }

    /**
     * Check to see if the save can proceed, and pop up errors if it
     * can't.
     */
    protected boolean checkSaveOk ()
    {
        String name = _scene.getName();
        String type = _scene.getType();

        String err = "";
        if (name.equals("") || name.indexOf("<") != -1 ||
            name.indexOf(">") != -1) {
            err += "Invalid scene name.\n";
        }

        if (type.equals("")) {
            err += "No scene type specified.\n";
        }

        if ("".equals(err)) {
            return true;
        } else {
            JOptionPane.showMessageDialog(
                this, err + "\nPlease fix and try again.",
                "Won't save: scene is broken!",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Present the user with a save file dialog and save the scenes to
     * the selected file.
     */
    public void handleSaveAs (ActionEvent evt)
    {
        _chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        _main.remove(_svpanel);
        _main.add(_chooser, BorderLayout.CENTER);
        SwingUtil.refresh(_main);
    }

    /**
     * Keeps our file path around and conveys that information in the
     * window title.
     */
    protected void setFilePath (String filepath)
    {
        _filepath = filepath;
        setTitle("Narya Scene Editor: " +
                 (_filepath == null ? "<new>" : _filepath));
    }

    /**
     * Handles a request to quit. Presently this just quits, but
     * eventually it should give the user the chance to save any edits in
     * progress.
     */
    public void handleQuit (ActionEvent evt)
    {
        System.exit(0);
    }

    /**
     * Handles a request to reload the test tiles.
     */
    public void handleTestTiles (ActionEvent evt)
    {
        // don't instantiate a testLoader until we actually need one
        if (_testLoader == null) {
            _testLoader = new TestTileLoader();
        }

        _tpanel.insertTestTiles(_testLoader.loadTestTiles());
    }

    /**
     * Update the mini view in the scrollbox.
     */
    public void updateMiniView (ActionEvent evt)
    {
        _scrollBox.updateView();
    }

    public void undo (ActionEvent evt)
    {
        _svpanel.undo();
    }

    public void redo (ActionEvent evt)
    {
        _svpanel.redo();
    }

    public void updateTileInfo ()
    {
        _tpanel.updateTileTable();
    }

    /**
     * Handles a request to open the preferences dialog.
     */
    public void handlePreferences (ActionEvent evt)
    {
        PreferencesDialog pd = new PreferencesDialog();
        EditorDialogUtil.display(this, pd);
    }

    /**
     * Make the currently selected base tile into the scene's default
     * tile.
     */
    public void handleSetDefBase (ActionEvent evt)
    {
        if (_model.getTileSet() instanceof BaseTileSet) {
            _svpanel.updateDefaultTileSet(_model.getTileSetId());
        } else {
            log.warning("Not making non-base tileset into default " +
                        _model.getTileSet() + ".");
        }
    }

    /** Handles JFileChooser responses when opening files. */
    protected ActionListener _openListener = new ActionListener() {
        public void actionPerformed (ActionEvent event) {
            String cmd = event.getActionCommand();

            // restore the scene view
            _main.remove(_chooser);
            _main.add(_svpanel);
            SwingUtil.refresh(_main);

            // load up the scene if they selected one
            if (cmd.equals(JFileChooser.APPROVE_SELECTION)) {
                File filescene = _chooser.getSelectedFile();
                switch (_chooser.getDialogType()) {
                case JFileChooser.OPEN_DIALOG:
                    openScene(filescene.getPath());
                    EditorConfig.config.setValue("editor.last_dir", filescene.getParent());
                    EditorConfig.config.setValue("editor.last_file", filescene.getAbsolutePath());
                    break;

                case JFileChooser.SAVE_DIALOG:
                    setFilePath(filescene.getPath());
                    handleSave(null);
                    break;

                default:
                    log.warning("Wha? Weird dialog type " + _chooser.getDialogType() + ".");
                    break;
                }
            }

            // oh god the hackery; on linux at least, the focus seems
            // to be hosed after we hide the chooser and we can't just
            // request to move it somewhere useful because it just
            // ignores us; so instead we have to wait for the current
            // event queue to flush before we can transfer focus
            EventQueue.invokeLater(new Runnable() {
                public void run () {
                    _sceneInfoPanel.requestFocusInWindow();
                }
            });
        }
    };

    /** The scene currently undergoing edit. */
    protected StageScene _scene;

    /** The file last associated with the current scene. */
    protected String _filepath;

    /** Contains the scene view panel or other fun stuff. */
    protected JPanel _main;

    /** Used for displaying dialogs. */
    protected JInternalFrame _dialog;

    /** The file chooser used for loading and saving scenes. */
    protected JFileChooser _chooser;

    /** The panel that displays the scene view. */
    protected EditorScenePanel _svpanel;

    /** The panel that displays tile info. */
    protected TileInfoPanel _tpanel;

    /** The panel that displays scene info. */
    protected SceneInfoPanel _sceneInfoPanel;

    /** The scrollbox used to display the view position within the scene. */
    protected EditorScrollBox _scrollBox;

    /** The editor data model. */
    protected EditorModel _model;

    /** The editor context. */
    protected EditorContext _ctx;

    /** We use this to load scenes. */
    protected SceneParser _parser = new StageSceneParser();

    /** We use this to save scenes. */
    protected StageSceneWriter _writer;

    /** The test tileset loader. */
    protected TestTileLoader _testLoader;
}
