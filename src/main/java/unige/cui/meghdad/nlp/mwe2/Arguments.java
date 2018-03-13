package unige.cui.meghdad.nlp.mwe2;
import org.apache.commons.cli.*;

/**
 * @author Meghdad Farahmand (meghdad.farahmand@gmail.com)
 * @date 13.03.18
 */
public class Arguments {

    public Args getArgs(String[] input_args) throws ParseException{

        Args args = new Args();


        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        CommandLine cmd = parser.parse(options, input_args);

        options.addOption("p2candidates", true, "Path 2 Not POS Tagged Candidates");
        options.addOption("p2corpus", true, "Path 2 POS tagged corpus");
        options.addOption("p2wr", true, "Path 2 word representations");
        options.addOption("size", true, "Size/length of word representations");
        options.addOption("k", true, "Number of neighbors");

        //optional options:
        options.addOption("rc", true, "Model: m1 or m2.");
        options.addOption("maxRank", true, "Return MWEs up to this rank.");


        //initialize options to default values and check if the required options are set
        if (!cmd.hasOption("p2corpus")) {
            System.out.println("Path to the POS tagged corpus must be set.");
            System.exit(1);
        }

        if (!cmd.hasOption("p2wr")) {
            System.out.println("A valid word representation must be specified.");
            System.exit(1);
        }

        if (!cmd.hasOption("p2candidates")) {
            System.out.println("A valid candidate list must be specified.");
            System.exit(1);
        }


        if (cmd.hasOption("maxRank")) {
            args.maxRank = Integer.parseInt(cmd.getOptionValue("maxRank"));
        }


        if (cmd.hasOption("rc")) {
            args.model = cmd.getOptionValue("rc");
        }


        if (cmd.hasOption("size")) {
            args.rl = Integer.parseInt(cmd.getOptionValue("size"));
        }else{
            System.out.println("Size/length of word representations must be specified.");
            System.exit(1);
        }


        if (cmd.hasOption("k")) {
            args.k = Integer.parseInt(cmd.getOptionValue("k"));
        }


        args.p2corpus = cmd.getOptionValue("p2corpus");
        args.p2candidates = cmd.getOptionValue("p2candidates");
        args.p2wr = cmd.getOptionValue("p2wr");


        return args;
    }

}

class Args{

    public String p2corpus;
    public String p2candidates;
    public String p2wr;


    public int k;
    public int maxRank=100;
    public String model = "m2";
    public int rl = -1;


}


