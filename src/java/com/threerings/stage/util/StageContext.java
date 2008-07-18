//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/vilya/
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

package com.threerings.stage.util;

import com.threerings.resource.ResourceManager;
import com.threerings.util.KeyDispatcher;
import com.threerings.util.KeyboardManager;
import com.threerings.util.MessageManager;

import com.threerings.media.FrameManager;
import com.threerings.media.image.ColorPository;
import com.threerings.media.image.ImageManager;

import com.threerings.cast.ComponentRepository;
import com.threerings.miso.util.MisoContext;

/**
 * A context that provides for the myriad requirements of the Stage
 * system.
 */
public interface StageContext
    extends MisoContext
{
    /**
     * Returns the frame manager driving our interface.
     */
    public FrameManager getFrameManager ();

    /**
     * Returns the resource manager via which all client resources are
     * loaded.
     */
    public ResourceManager getResourceManager ();

    /**
     * Access to the image manager.
     */
    public ImageManager getImageManager ();

    /**
     * Provides access to the key dispatcher.
     */
    public KeyDispatcher getKeyDispatcher ();

    /**
     * Returns a reference to the message manager used by the client.
     */
    public MessageManager getMessageManager ();

    /**
     * Returns a reference to the keyboard manager.
     */
    public KeyboardManager getKeyboardManager();

    /**
     * Returns the component repository in use by this client.
     */
    public ComponentRepository getComponentRepository ();

    /**
     * Returns a reference to the colorization repository.
     */
    public ColorPository getColorPository ();
    
    /**
     * Translates the specified message using the default bundle.
     */
    public String xlate (String message);

    /**
     * Translates the specified message using the specified bundle.
     */
    public String xlate (String bundle, String message);
}
