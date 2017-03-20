using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;

class Program
{
    public static void Main(params string[] args)
    {
        File.ReadAllLines(args[0])
            .Skip(1)
            .Select(rad => rad.Split(';').Skip(2).ToArray())
            .Select(n => new {Namn = $"{n[0]} {n[1]}", Klubb = n[2], Man = n[3] == "H", Tid = TimeSpan.Parse(n[4])})
            .GroupBy(åkare => åkare.Klubb)
            .SelectMany(klubb => SkapaKlubblag(klubb.OrderBy(åkare => åkare.Tid)))
            .OrderBy(lag => lag.Totaltid)
            .Select((lag, i) => new { Placering = i+1, lag.Klubb, Medlemmar = ((List<string>) lag.Medlemmar).Aggregate((p, c) => $"{p}, {c}"), lag.Totaltid })
            .ToList().ForEach(n => Console.WriteLine($"{n.Placering}: {n.Klubb} ({n.Medlemmar}) Tid: {n.Totaltid}"));
    }

    private static IEnumerable<dynamic> SkapaKlubblag(IEnumerable<dynamic> klubbmedlemmar)
    {
        var klubblista = klubbmedlemmar.ToList();
        while (klubblista.Any())
        {
            var team = new List<dynamic>();

            foreach (var åkare in klubblista.ToList())
            {
                if (team.Count == 2 && (team.All(i => i.Man) && åkare.Man || team.All(i => !i.Man) && !åkare.Man))
                    continue;

                team.Add(åkare);
                klubblista.Remove(åkare);

                if (team.Count == 3)
                {
                    yield return new
                    {
                        Klubb = team[0].Klubb,
                        Medlemmar = team.Select(n => (string)n.Namn).ToList(),
                        Totaltid = team.Select(n => n.Tid).Aggregate(TimeSpan.Zero, (p, c) => p + c)
                    };

                    break;
                }
            }
        }
    }
}
