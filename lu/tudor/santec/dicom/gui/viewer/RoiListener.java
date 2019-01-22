package lu.tudor.santec.dicom.gui.viewer;

import java.util.EventListener;


public interface RoiListener extends EventListener
{
  void update( String RoiEventName );
}