package com.seedcloud.dc.doccleaners;
import java.io.IOException;

public interface DocumentCleaner {
	String clean(String input)throws IOException ;
}
