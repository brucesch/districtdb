<?php

/**
* Form PHP plugin helper that is used to add or edit Acymailing entries
* for the District Newsletter when a Persons entry is added or edited.
* @package Joomla
* @subpackage Fabrik
* @author Bruce Schurmann
* @copyright (C) Bruce Schurmann
* @license http://www.gnu.org/copyleft/gpl.html GNU/GPL
*/

/// Check to ensure this file is included in Joomla!
defined('_JEXEC') or die();

// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
// NB. This PHP Form plugin helper must be run after the JUser plugin to work correctly!!
//     (If the JUser plugin is used on this form.)
// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


jimport('joomla.application.component.helper');

// -----------------  Edit these config variables to match your setup.

// This var is the Label that you defined for the table that the user registers with.
$userProfileTableLabel = "People";
// This is the name of the db table that is for people.
$tableName = 'districtdb_persons';
// This is the name of the 'newsletter' field in the above db table.
$newsfld  = 'newsletter';                          
// This is the name of the 'email' field in the above db table.
$emailfld  = 'email';                
// This is the name of the 'name' field in the above db table.
$namefld  = 'name';                
// This is the name of the 'userid' field in the above db table.
$useridfld = 'userid';
// This is the primary key (listid) of the Acymailing list table for the Newsletter.
$newslistid = 2;          

// -----------------  End of config.

// DO NOT EDIT BELOW THIS LINE.

// Check to see if Acymailing is installed. If not then nothing to do!
$acyinstalled = JComponentHelper::isEnabled('com_acymailing', true);


if ($acyinstalled) {
    // Grab the email address for this user from the form
    $emailval  = $formModel->_formData[$tableName.'___'.$emailfld];

    // Grab the full name for this user from the form
    $nameval  = $formModel->_formData[$tableName.'___'.$namefld];

    // Grab the optional userid (Joomla id) for this user from the form
    $useridval  = $formModel->_formData[$tableName.'___'.$useridfld.'_raw'];
    if (!$useridval) {
        $useridval = 0;
    }

    // Does the user already have an Acymailing profile?
    $whereclause = " WHERE LOWER(`email`)=LOWER('".$emailval."')";
    $dbo = &JFactory::getDBO();
    $dbo->setQuery("SELECT `subid`,`confirmed`,`enabled`,`accept` FROM `#__acymailing_subscriber` ".$whereclause);
    $acyinfo = $dbo->loadRow();
    $acystatus = "";
    if ($acyinfo) {
        $whereclause = " WHERE `listid`=".$newslistid." AND `subid`=".$acyinfo[0];
        $dbo->setQuery("SELECT `status` FROM `#__acymailing_listsub` ".$whereclause);
        $acystatus = $dbo->loadResult();
    }
    
    // Are we on the "Profile" form or the "People" form.
    // This tells us if we have the user editing their own info or a 
    // staffer working on the person's info.
    if ($userProfileTableLabel == $formModel->getLabel()) {
        $userProfile = true;
    } else {
        $userProfile = false;
    }

    // Does the form have the Newsletter request == yes?
    $newsval = $formModel->_formData[$tableName.'___'.$newsfld];
    // may be a radiobutton which comes back as an array
    if (is_array($newsval)) {
        $newsval = $newsval[0];
    }
    
    // Now get down to business
    if ($userProfile) {
        // This is the user editing her profile
        if (!$acyinfo) {
            // User is not already in the Acy system.
            if (!$newsval) {
                // User does not want the Newsletter
                $acynewvals = "0,1,1";
                $acynewstat = "-1";
            } else {
                // Use does want the Newsletter
                $acynewvals = "1,1,1";
                $acynewstat = "1";
            }
        } else {
            // User is already in the Acy system.
            if (!$newsval) {
                // User does not want the Newsletter
                $acynewvals = "";
                $acynewstat = "-1";
            } else {
                // Use does want the Newsletter
                // Was the user's Acy previously disabled?
                if (!$acyinfo[2]) {
                    $acynewvals = $acyinfo[1].",0,1";
                    $acynewstat = "1";
                } else {
                    // Check confirmed status
                    if ($acyinfo[1]) {
                        $acynewvals = "1,1,1";
                        $acynewstat = "1";
                    } else {
                        //$acynewvals = "0,1,1";
                        //$acynewstat = "2";
                        $acynewvals = "1,1,1";
                        $acynewstat = "1";
                    }
                }
            }
        }
    } else {
        // This is a staffer editing another user's profile
        if (!$acyinfo) {
            // User is not already in the Acy system.
            if (!$newsval) {
                // User does not want the Newsletter
                $acynewvals = "0,1,1";
                $acynewstat = "-1";
            } else {
                // Use does want the Newsletter
                $acynewvals = "1,1,1";
                $acynewstat = "1";
            }
        } else {
            // User is already in the Acy system.
            if (!$newsval) {
                // User does not want the Newsletter
                $acynewvals = "";
                $acynewstat = "-1";
            } else {
                // User does want the Newsletter
                // Had the user previously unsubscribed from Newsletter?
                if ($acystatus == -1) {
                    $acynewvals = "";
                    $acynewstat = "-1";
                } else {
                    // Had the user previously unsubscribed from all Acymailings?
                    if (!$acyinfo[3]) {
                        $acynewvals = $acyinfo[1].",".$acyinfo[2].",0";
                        $acynewstat = "";
                    } else {
                        $acynewvals = "1,1,1";
                        $acynewstat = "1";
                    }
                }
            }
        }
    }
                 

    // Now wrap up by updating the Acymailing info
    // Prep the data array
    $acynewvalsarray = split(',', $acynewvals);
    // "Dates" in Acymailing
    $now = time();
    // Update Acymailing subscriber info
    if ($acynewvals) {
        $setclause = " SET `confirmed`=".$acynewvalsarray[0].", `enabled`=".$acynewvalsarray[1].", `accept`=".$acynewvalsarray[2];
        $setclause .= ", `email`='".$emailval."', `userid`=".$useridval.", `name`='".$nameval."', `html`=1";
        if ($acyinfo) {
            // Updating an existing record
            $whereclause = " WHERE `subid`=".$acyinfo[0];
            $dbo->setQuery("UPDATE `#__acymailing_subscriber` ".$setclause.$whereclause);
            $dbo->query();
        } else {
            // Inserting a new record
            $dbo->setQuery("INSERT INTO `#__acymailing_subscriber` ".$setclause.", `created`=".$now);
            $dbo->query();
            $newsubid = $dbo->insertid();
        }
    }
    // Update Acymailing listsub info
    if ($acynewstat) {
        if ($acynewstat == -1) {
            $dateclause = ", `unsubdate`=".$now;
        } else {
            $dateclause = ", `subdate`=".$now;
        }
        $setclause = " SET `status`=".$acynewstat.$dateclause;
        if ($acystatus) {
            // Updating an existing record
            $whereclause = " WHERE `listid`=".$newslistid." AND `subid`=".$acyinfo[0];
            $dbo->setQuery("UPDATE `#__acymailing_listsub` ".$setclause.$whereclause);
            $dbo->query();
        } else {
            // Inserting a new record
            if ($acyinfo) {
                $fsubid = $acyinfo[0];
            } else {
                $fsubid = $newsubid;
            }
            $dbo->setQuery("INSERT INTO `#__acymailing_listsub` ".$setclause.", `listid`=".$newslistid.", `subid`=".$fsubid);
            $dbo->query();
        }
    }
}
?>