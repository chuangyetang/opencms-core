/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/xml/page/TestCmsXmlPageInSystem.java,v $
 * Date   : $Date: 2004/08/03 07:19:03 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
package org.opencms.xml.page;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.types.CmsResourceTypeXmlPage;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.I_CmsConstants;
import org.opencms.test.OpenCmsTestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the XML page that require a running OpenCms system.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 5.5.0
 */
public class TestCmsXmlPageInSystem extends OpenCmsTestCase {
   
    private static final String UTF8 = CmsEncoder.C_UTF8_ENCODING;
        
    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {

        TestSuite suite = new TestSuite();
        
        suite.addTest(new TestCmsXmlPageInSystem("testLinkReplacement"));
        
        TestSetup wrapper = new TestSetup(suite) {

            protected void setUp() {
                setupOpenCms("simpletest", "/sites/default/");
            }

            protected void tearDown() {
                removeOpenCms();
            }

        };

        return wrapper;
    }
    
    /**
     * Default JUnit constructor.<p>
     * 
     * @param arg0 JUnit parameters
     */    
    public TestCmsXmlPageInSystem(String arg0) {
        super(arg0);
    }
    
    /**
     * Tests XML link replacement.<p>
     * 
     * @throws Exception if something goes wrong
     */
    public void testLinkReplacement() throws Exception {

        CmsObject cms = getCmsObject();
        echo("Testing XML page link replacement");

        String filename = "/folder1/subfolder11/test1.html";
        String content = CmsXmlPageFactory.createDocument(Locale.ENGLISH, UTF8);
        List properties = new ArrayList();
        properties.add(new CmsProperty(I_CmsConstants.C_PROPERTY_CONTENT_ENCODING, UTF8, null));
        properties.add(new CmsProperty(I_CmsConstants.C_PROPERTY_LOCALE, Locale.ENGLISH.toString(), null));        
        properties.add(new CmsProperty(CmsXmlPage.C_PROPERTY_ALLOW_RELATIVE, String.valueOf(false), null));        
        cms.createResource(filename, CmsResourceTypeXmlPage.C_RESOURCE_TYPE_ID, content.getBytes(UTF8), properties);
        
        CmsFile file = cms.readFile(filename);
        CmsXmlPage page = CmsXmlPageFactory.unmarshal(cms, file);
        String element = "test";
        page.addValue(element, Locale.ENGLISH);
        String text;
        
        // test link replacement with existing file
        page.setStringValue(cms, element, Locale.ENGLISH, "<a href=\"index.html\">link</a>");                
        text = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals("<a href=\"/data/opencms/folder1/subfolder11/index.html\">link</a>", text);

        // test link replacement with non-existing file
        page.setStringValue(cms, element, Locale.ENGLISH, "<a href=\"index_noexist.html\">link</a>");                
        text = page.getStringValue(cms, element, Locale.ENGLISH);
        assertEquals("<a href=\"/data/opencms/folder1/subfolder11/index_noexist.html\">link</a>", text);
    }
}
