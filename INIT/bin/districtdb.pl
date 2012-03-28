#!/usr/bin/perl
#
# bruces
#
# This script does two things:
# 1. It will take a 'mysqldump' of all the Fabrik db tables from my test server
#    and will "tokenize" the dump in preparation for #2.
# 2. Take a "tokenized" version of the Fabrik db tables and, using the token
#    substitution values defined in the configuration file, generate a new
#    db dump file that is reday for import into a fresh server.
#
#



use Getopt::Std;
use Data::Dumper;
use Cwd; # module for finding the current working directory
use Carp;
use Text::CSV_XS;               # General CSV utils
$csv = Text::CSV_XS->new();     # reused below
use File::Copy::Recursive qw(fcopy rcopy dircopy fmove rmove dirmove);
use File::Find;

# --------------------------  Constants

# Bump this as this program undergoes major revs.
$VERSION = "2.0";

# Used in the tokenizer to map current ACCESS LEVELs
%ALEVEL = (
    '4' => '%ALVLNOBODY%',
    '5' => '%ALVLFRIEND%',
    '6' => '%ALVLCOMMITTEE%',
    '7' => '%ALVLSUPERVISOR%',
    '8' => '%ALVLDISTADMIN%'
    );

# Sanity checking for re-tokeinzing purposes
%CHKCNT = (
    '%ALVLCOMMITTEE%'  =>  23,
    '%ALVLFRIEND%'  =>  13,
    '%ALVLNOBODY%'  =>  59,
    '%ALVLSUPERVISOR%'  =>  12,
    '%ARCHIVEEMAIL%'  =>  2,
    '%BOUNCEEMAIL%'  =>  2,
    '%DBPREFIX%'  =>  79,
    '%DEFAULTLATITUDE%'  =>  5,
    '%DEFAULTLONGITUDE%'  =>  5,
    '%NEWSLETTERID%'  =>  1,
    );

# --------------------------  End Constants

# Here we go
getopts('hdc:pgo:');

if (defined($opt_h)) {
    print STDERR "\n";
    print STDERR "$0 [-h] [-p|-g] [-c configfile] -o sql_outfile sql_infile\n";
    print STDERR "where: \n";
    print STDERR "  -p|-g -p is the first pass, 'prep'\n";
    print STDERR "        -g is the second pass, 'generate'\n";
    print STDERR "  'configfile' is the token value file, required if using -g\n";
    print STDERR "  'sql_infile' is the input file for processing\n";
    print STDERR "  'sql_outfile' is output file\n";
    print STDERR "\n";

    exit;
}
if (defined($opt_d)) {
    $DEBUG = 1;
}
if ((defined($opt_p) && defined($opt_g)) || (!defined($opt_p) && !defined($opt_g))) {
    print STDERR "# ERROR: you must specify one of -p (prep) or -g (generate)\n";
    exit;
}
if (defined($opt_g) && !defined($opt_c)) {
    print STDERR "# ERROR: you requested -g, generate, but are missing config file (-c)\n";
    exit;
}
if (!defined($opt_o)) {
    print STDERR "# ERROR: missing an output file (-o)\n";
    exit;
}


# Get a timestamp - you never know
($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime(time);
$year += 1900;
$mon += 1;
$datestr = sprintf("%4d-%02d-%02d %02d:%02d:%02d",$year,$mon,$mday,$hour,$min,$sec);

# Are we doing the 'prep' (tokenize) or the 'generate' (final sql for a site)
if (defined($opt_p)) {
    
    # PREP STEP 0
    # Need to split the long INSERT statements into more manageable chunks

    # Create a temp file
    open(SQLOUTTMP,">${opt_o}.tmp") || Carp::croak("Problem opening temp file for write: ${opt_o}.tmp");

    # Now the big loop (0)
    while(<>) {
	chomp;
        s/ VALUES \(/ VALUES\n(/;
	s/\),\(/),\n(/g;
	print SQLOUTTMP "$_\n";
    }

    close SQLOUTTMP;

    # PREP STEP 1
    # Now tokenize

    # Open the output file
    open(SQLOUT,">$opt_o") || Carp::croak("Problem opening '-o sql_outfile': $opt_o");

    # Timestamp and version code the output file
    print SQLOUT "-- Fabrik auto-generation\n";
    print SQLOUT "-- $0\n";
    print SQLOUT "-- Bruce Schurmann\n";
    print SQLOUT "-- $datestr\n";
    print SQLOUT "-- Version: $VERSION\n";
    print SQLOUT "-- PREP STEP\n";
    print SQLOUT "-- \n\n";


    # Now the big loop (1)
    open(SQLOUTTMP,"${opt_o}.tmp") || Carp::croak("Problem opening temp file for read: ${opt_o}.tmp");
    while(<SQLOUTTMP>) {
	chomp;

	# Track which table we are processing - this may be useful in the heuristics below
	if (/^LOCK TABLES `(.*)`/) {
	    # Trim the database table prefix
	    ($table = $1) =~ s/^[^_]*_//;
	    # print STDOUT "table: $table\n";
	    $record = 1;
	}
	# Record tracker - also for heuristics if needed
        if (/^\(([^,]*),/) {
	    $record = $1;
	    if ($table eq 'fabrik_lists') {
		# print STDOUT "record: $record\n";
	    }
	}

	# Now start tokenizing

        # email addresses - used in the Email function (Persons and Service)
        $CNT{'%BOUNCEEMAIL%'} += s/bounce\@dadhead\.schurmann\.org/%BOUNCEEMAIL%/g;
        $CNT{'%ARCHIVEEMAIL%'} += s/archive\@schurmann\.org/%ARCHIVEEMAIL%/g;

        # Default map start coords - used in the googlemap plugin for Units, etc.
        $CNT{'%DEFAULTLATITUDE%'} += s/30\.250309/%DEFAULTLATITUDE%/g;
        $CNT{'%DEFAULTLONGITUDE%'} += s/-97\.759783/%DEFAULTLONGITUDE%/g;

	# Access Levels - this is a biggy
	# Only an issue on the Lists
	if ($table eq 'fabrik_lists') {

	    # The "View list" top-level access control is actually a field, 'access', in
	    # the schema - the 19th field
            if (/^\(((?:[^,]+,){18})([^,]+),/) {
		$viewlistaccess = $2;
		if (exists($ALEVEL{"$viewlistaccess"})) {
		    # print STDOUT "list: $table  found access: " . $ALEVEL{"$viewlistaccess"} . "\n";
		    # print STDOUT "  all:   $1\n";
		    $newval = "(" . $1 . $ALEVEL{"$viewlistaccess"} . ",";
		    $CNT{$ALEVEL{"$viewlistaccess"}} += s/^\(((?:[^,]+,){18})([^,]+),/$newval/e;
		} else {
		    print STDOUT "WARNING: list: $table  not found access: $viewlistaccess\n";
		}
	    }
	    # Phew - the rest of the List Access Levels are pretty easy
	    # view records       \"allow_view_details\":\"4\"
	    if (/\\"allow_view_details\\":\\"([0-9]+)\\"/) {
		$access = $1;
		if (exists($ALEVEL{"$access"})) {
		    $newval = '\"allow_view_details\":\"' . $ALEVEL{"$access"} . '\"';
		    $CNT{$ALEVEL{"$access"}} += s/\\"allow_view_details\\":\\"([0-9]+)\\"/$newval/e;
		}
	    }
	    # edit records       \"allow_edit_details\":\"4\"
	    if (/\\"allow_edit_details\\":\\"([0-9]+)\\"/) {
		$access = $1;
		if (exists($ALEVEL{"$access"})) {
		    $newval = '\"allow_edit_details\":\"' . $ALEVEL{"$access"} . '\"';
		    $CNT{$ALEVEL{"$access"}} += s/\\"allow_edit_details\\":\\"([0-9]+)\\"/$newval/e;
		}
	    }
	    # add records       \"allow_add\":\"4\"
	    if (/\\"allow_add\\":\\"([0-9]+)\\"/) {
		$access = $1;
		if (exists($ALEVEL{"$access"})) {
		    $newval = '\"allow_add\":\"' . $ALEVEL{"$access"} . '\"';
		    $CNT{$ALEVEL{"$access"}} += s/\\"allow_add\\":\\"([0-9]+)\\"/$newval/e;
		}
	    }
	    # delete records       \"allow_delete\":\"4\"
	    if (/\\"allow_delete\\":\\"([0-9]+)\\"/) {
		$access = $1;
		if (exists($ALEVEL{"$access"})) {
		    $newval = '\"allow_delete\":\"' . $ALEVEL{"$access"} . '\"';
		    $CNT{$ALEVEL{"$access"}} += s/\\"allow_delete\\":\\"([0-9]+)\\"/$newval/e;
		}
	    }
	    # empty records       \"allow_drop\":\"4\"
	    if (/\\"allow_drop\\":\\"([0-9]+)\\"/) {
		$access = $1;
		if (exists($ALEVEL{"$access"})) {
		    $newval = '\"allow_drop\":\"' . $ALEVEL{"$access"} . '\"';
		    $CNT{$ALEVEL{"$access"}} += s/\\"allow_drop\\":\\"([0-9]+)\\"/$newval/e;
		}
	    }
	    # csv import       \"csv_import_frontend\":\"4\"
	    if (/\\"csv_import_frontend\\":\\"([0-9]+)\\"/) {
		$access = $1;
		if (exists($ALEVEL{"$access"})) {
		    $newval = '\"csv_import_frontend\":\"' . $ALEVEL{"$access"} . '\"';
		    $CNT{$ALEVEL{"$access"}} += s/\\"csv_import_frontend\\":\\"([0-9]+)\\"/$newval/e;
		}
	    }
	    # csv export       \"csv_export_frontend\":\"4\"
	    if (/\\"csv_export_frontend\\":\\"([0-9]+)\\"/) {
		$access = $1;
		if (exists($ALEVEL{"$access"})) {
		    $newval = '\"csv_export_frontend\":\"' . $ALEVEL{"$access"} . '\"';
		    $CNT{$ALEVEL{"$access"}} += s/\\"csv_export_frontend\\":\\"([0-9]+)\\"/$newval/e;
		}
	    }

	}

	# Joomla db prefix
	$CNT{'%DBPREFIX%'} += s/mrn7l/%DBPREFIX%/g;

        # Newsletter
        $CNT{'%NEWSLETTERID%'} += s/`#__acymailing_listsub`.`listid`=2 AND/`#__acymailing_listsub`.`listid`=%NEWSLETTERID% AND/g;

	# Chunk line
	print SQLOUT "$_\n";
    }

    close SQLOUTTMP;
    close SQLOUT;

    # Summarize what happened and sanity check
    SanityCheck(\%CNT, \%CHKCNT);


} else {

    # GENERATE STEP

    # process the config_file
    require $opt_c;

    # Open the output file
    open(SQLOUT,">$opt_o") || Carp::croak("Problem opening '-o sql_outfile': $opt_o");

    # Timestamp and version code the output file
    print SQLOUT "-- Fabrik auto-generation\n";
    print SQLOUT "-- $0\n";
    print SQLOUT "-- Bruce Schurmann\n";
    print SQLOUT "-- $datestr\n";
    print SQLOUT "-- Version: $VERSION\n";
    print SQLOUT "-- GENERATE STEP\n";
    print SQLOUT "-- Config: $opt_c\n";
    print SQLOUT "-- Config Version: $CVERSION\n";
    print SQLOUT "-- \n\n";

    # Now the big loop
    while(<>) {
	chomp;

	foreach $key (sort keys %VALS) {

            $val = $VALS{$key};
	    $CNT{$key} += s/$key/$val/eg;

	}

	print SQLOUT "$_\n";
    }

    close SQLOUT;
    
    # Summarize what happened and sanity check
    SanityCheck(\%CNT, \%CHKCNT);

    print STDOUT "\nAs a double check you should do this (there should be no matches):\n";
    print STDOUT "egrep '%[A-Z]%' $opt_o\n\n";

}


# -----------------  helpers

#
# Sanity Check
sub SanityCheck {

    my ($CNT, $CHKCNT) = @_;

    # Summarize what happened
    print STDOUT "Summary:\n";
    foreach $key (sort keys %$CNT) {
	print STDOUT "  $key  $CNT{$key}\n";
    }

    # Sanity check
    $ok = 1;
    foreach $key (sort keys %$CNT) {
	if (!exists($CHKCNT{$key})) {
	    print STDOUT "ERROR: new key - not in sanity check: $key\n";
	    $ok = 0;
	}
	if (exists($CHKCNT{$key}) && ($CNT{$key} != $CHKCNT{$key})) {
	    print STDOUT "ERROR: cnt value mismatch key: $key expected: " . $CHKCNT{$key} . " got: " . $CNT{$key} . "\n";
	    $ok = 0;
	}
    }
    foreach $key (sort keys %$CHKCNT) {
	if (!exists($CNT{$key})) {
	    print STDOUT "ERROR: missing key - in sanity check: $key\n";
	    $ok = 0;
	}
    }

    # Final msg
    if ($ok) {
	print STDOUT "\nCongratulations! All appears to be OK.\n(Warnings about 'not found access' for 1 or 2 is OK.)\n";
    } else {
	print STDOUT "\nOops! You should check the errors above.\n";
    }

}
