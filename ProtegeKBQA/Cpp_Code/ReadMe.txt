Here's the source for the filtering program.  Please find a spot for it in SVN. I'm also attaching a QAOutput.txt and a filterQA.txt files I've been using for testing.  Eventually, we'll also put in the production filterQA.txt file in SVN.

I'll go over it on Monday (we'll have an early meeting, start 12 noon).  It might go through some tweaks depending on the discussion, but somebody should still QA it independently of me.

If you still have gcc locally you can compile and further test locally if you wish (or if missing gcc let me know and I'll send the binary).  We only have c++ compilers in cbiows501, so for production work it needs to be compiled there and then copied to QA, dev, etc.  To compile in linux, just

	g++ diffDetail.cpp -o diffDetail -Wall

& to run afterwards either put in bin, sbin, or somewhere in the path, or run as ./diffDetail <arg-list>
