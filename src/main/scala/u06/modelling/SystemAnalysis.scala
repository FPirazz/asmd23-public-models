package u06.modelling

// Basical analysis helpers
object SystemAnalysis:

  type Path[S] = List[S]

  extension [S](system: System[S])

    def normalForm(s: S): Boolean = system.next(s).isEmpty

    def complete(p: Path[S]): Boolean = normalForm(p.last)

    // paths of exactly length `depth`
    def paths(s: S, depth: Int): Seq[Path[S]] = depth match
      case 0 => LazyList()
      case 1 => LazyList(List(s))
      case _ =>
        for
          path <- paths(s, depth - 1)
          next <- system.next(path.last)
        yield path :+ next

    // complete paths with length '<= depth' (could be optimised)
    def completePathsUpToDepth(s: S, depth:Int): Seq[Path[S]] =
      (1 to depth).to(LazyList) flatMap (paths(s, _)) filter (complete(_))

    // Task 1
    // Implemented previous method with filtering to remove duplicates
    // This is done to permit computers to calculate paths in resonable time
    def pathsFiltered(s: S, depth: Int, filter: Path[S] => Boolean): Seq[Path[S]] = depth match
      case 0 => LazyList()
      case 1 => LazyList(List(s))
      case _ =>
        for
          path <- pathsFiltered(s, depth - 1, filter)
          next <- system.next(path.last) if filter(path :+ next)
        yield path :+ next

    def completePathsUpToDepthFiltered(s: S, depth: Int, filter: Path[S] => Boolean): Seq[Path[S]] =
      (1 to depth).to(LazyList) flatMap (pathsFiltered(s, _, filter)) filter (complete(_))