import java.io._
import collection.mutable.HashMap
import scala.collection.mutable.ListBuffer


class Convert(val f: Feed_In) {

   var colors = Map("Entity" -> Array("N","<strong>","","","</strong>"), "Twitter Username" -> Array("Y","""<a href="http://twitter.com/""","","""">""","</a>"),
      "Link" -> Array("Y","""<a href="""","","""">""","</a>"))
   var greeting: String = ""
   var curr_decode=""
   var curr_encode:Array[String] = new Array[String](5)// My Encoding scheme . It should be of a size 5.
   val builder = StringBuilder.newBuilder
   var out_bound = false

   def addmap(key: String){
      colors = colors + (key -> curr_encode)
   }

   def add( key: String, pos1: Int, pos2: Int){
      if (colors.contains(key)){
         if(colors(key)(0)=="N"){
            greeting = colors(key)(1)+f.str.substring(pos1,pos2)+colors(key)(4)
         }
         else{
            greeting = colors(key)(1)+colors(key)(2)+f.str.substring(pos1,pos2)+colors(key)(3)+f.str.substring(pos1,pos2)+colors(key)(4)
         }
      }
   }

   def decoder(key: String){
         var temp:String = ""
         if(colors(key)(0)=="Y"){
            temp="INPUT"
         }
         curr_decode = "Px"+colors(key)(1)+colors(key)(2)+temp+colors(key)(3)+"Sx"+colors(key)(4)
   }

   def encoder(key: String, value: String){
      val string: String = value
      val k :Int = string indexOf "INPUT"
      var z:Array[String] = new Array[String](5)
      val k1 :  Int = value indexOf "Sx"
      var prefx :Int = value indexOf "Px"
      var suffx : Int = value indexOf "Sx"
      if (k != -1){
         z(0)="Y"
         z(1)=value.substring(prefx + 2,k)
         z(2)=""
         z(3)=value.substring(k+5,k1 - 1)
         z(4)=value.substring(suffx+2,value.length)
      }
      else{
         z(0)="N"
         z(1)=value.substring(prefx+2,k1 )
         z(2)=""
         z(3)=""
         z(4)=value.substring(suffx+2,value.length )



      }
      curr_encode = z
      
   }

   def out_of_bounds(){
      for (in <- f.enlist){     
         if((in.x>f.str.length) || (in.y>f.str.length)){
            out_bound=true
         }
      }
   }

   def Convert1(){
      var start: Int = 0
      var end: Int = 0
      for (in <- f.enlist.sortWith(_.x<_.x)){
         //Untagged appends
         start=end
         end=in.x
         builder.append(f.str.substring(start,end))
         //Tagged appends
         start = in.x
         end = in.y
         add(in.type1,in.x,in.y)
         builder.append(greeting)
      }
      start=end
      end=f.str.length()
      builder.append(f.str.substring(start,end))
   }


   def stringarr(s: String){
      //var z:Array[String] = s.split(" ");
      for ( x <- s ) {
         println(x)
      }
   }
}

//Feed is the actual input and 
class Feed_In(val s1: String){
      var str: String = s1
      //val Entrylist = List[Entry]()
      val enlist = ListBuffer[Entry]()
      var overlap = false

      def add_to_list(k: Entry){
          enlist+=k
      }
      def show_contents(){
            println("Feed:"+str)   
            for ( in <- enlist.sortWith(_.x<_.x) ) {
               println("Type: "+in.type1+", Start: "+in.x+" End: ", in.y)
            }
      }

      def find_overlaps(){
            var start = 0
            var end = 0
            for ( in <- enlist.sortWith(_.x<_.x) ) {
               start= in.x
               if (start< end){
                  overlap = true
               }
               end=in.y
            }
      }

}

// Entity is the type of String and its positions
class Entry(val s1: String, val xc:Int,val yc: Int){
      var x: Int = xc
      var y: Int = yc
      var type1: String = s1
}


class unit_tests(){

      var k: String = "Obama visited Facebook headquarters: http://bit.ly/xyz @elversatile";
      val ent = new Entry("Entity",14,22)
      val ent1 = new Entry("Entity",0,5)
      val ent2 = new Entry("Twitter Username",56,67)
      val ent3 = new Entry("Link",37,54)
      val Feed_In = new Feed_In(k)
      Feed_In.add_to_list(ent);Feed_In.add_to_list(ent1);Feed_In.add_to_list(ent2);Feed_In.add_to_list(ent3);
      Feed_In.show_contents()
      val pt = new Convert(Feed_In);

   def basic_test(){
      pt.Convert1()
      println("Output: "+pt.builder)
   }

   def encoder_test(){
      pt.decoder("Link")
      print(pt.curr_decode+"  ------>  ")
      pt.encoder("Link",pt.curr_decode)
      print("(");for ( in <- pt.curr_encode){print(in+", ")  };print (")\n")
      pt.decoder("Entity")
      print(pt.curr_decode+"  ------>  ")
      pt.encoder("Entity",pt.curr_decode)
      print("(");for ( in <- pt.curr_encode){print(in+", ")  };print (")\n")
      pt.decoder("Twitter Username")
      print(pt.curr_decode+"  ------>  ")
      pt.encoder("Twitter Username",pt.curr_decode)
      print("(");for ( in <- pt.curr_encode){print(in+", ")  };print (")\n")
   }

   def overlap_test(){
      Feed_In.find_overlaps()
      println("Overlap:"+ Feed_In.overlap)
      println("Now I introduce (40,58) which is an overlap and will give bad results")
      val ent4 = new Entry("Link",40,58);
      Feed_In.add_to_list(ent4);
      Feed_In.find_overlaps()
      println("Overlap:"+ Feed_In.overlap)
   }

   def out_of_bounds_test(){
      pt.out_of_bounds()
      println("Out of bound" + pt.out_bound)
      println("Now I introduce (40,85) which is out bound and will give bad results")
      val ent4 = new Entry("Link",40,85);
      Feed_In.add_to_list(ent4);
      val pt1 = new Convert(Feed_In);
      pt.out_of_bounds()
      println("Out of bound:"+ pt.out_bound)
   }

   def introduce_hashtag(key: String,value : String){
      var k1: String = "Feeling Awesome #TwitterFeed";
      var ent8 = new Entry(key,17,28)
      val Feed_In1 = new Feed_In(k1)
      Feed_In1.add_to_list(ent8)
      Feed_In1.show_contents()
      val pt2 = new Convert(Feed_In1)
      pt2.encoder(key,value)
      print(value+ "    ------->  ")
      print("(");for ( in <- pt2.curr_encode){print(in+", ")  };print (")\n");
      //pt2.colors updated (key,pt2.curr_encode)// Add it to a HashMap
      pt2.addmap(key)
      println(pt2.colors.keys)
      pt2.Convert1()
      println("Output: "+pt2.builder)
   }

}

object Test {
   def main(args: Array[String]) {
      
      // Test 1: run the simple program
      println("\nTest 1 : Enter the tweet. This is the given example in the assignment")
      val check = new unit_tests()
      check.basic_test()


      //Test 2: Check for the encoding conversions
      println("\nTest2: Look at my encoding mechanism . This will serve use for future introductions")
      check.encoder_test()

      //Test 3: Check for the encoding conversions
      println("\nTest3: Overlaps are dangerous mistakes.If you see a result as true please run Test 1")
      check.overlap_test()
      
      //Test4 : Check for bounds
      println("\nTest4: Going out of bounds are bad mistakes . This test checks for both negative and positive indices")
      check.out_of_bounds_test()
      
      println("\nTest5: A very important aspect. Introducting a new element")
      check.introduce_hashtag("Hashtag","""Px<a href="https://twitter.com/hashtag/INPUT">Sx</a>""")



   }
}
