In Aubio there is a data type uint_t defined as unsigned int.
As in Java there exists no unsigned datatypes, there are two options:
1 - use long instead of int => however there might be problems with iterators or array-index-accesses
2 - keep using normal int => risk of overflow

We choose option 2 ;-)


Check out http://git.aubio.org/ for more information.