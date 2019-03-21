#include <iostream>
#include <i3d/image3d.h>

template <typename V>
void collisionReportingMakeAF(const i3d::Image3d<V>& fullImg, i3d::Image3d<V>& afImg);


int main(int argc,char* argv[])
{
	//params:
	//manTraImg manSegImg currentFrameNo desiredZ
	i3d::Image3d<i3d::GRAY16> manTra(argv[1]);
	i3d::Image3d<i3d::GRAY16> manSeg(argv[2]);
	const char* const currentFrameNo = argv[3];
	//const char* const desiredZ = argv[4];
	const int desiredZ = atol(argv[4]);

	i3d::Image3d<i3d::GRAY16> afMarker;
	collisionReportingMakeAF(manTra, afMarker);

	const i3d::GRAY16 *af = afMarker.GetFirstVoxelAddr();
	const i3d::GRAY16 *z  = manTra.GetVoxelAddr(0,0,desiredZ);
	const i3d::GRAY16 *ms = manSeg.GetFirstVoxelAddr();

	const size_t line = manSeg.GetWidth();
	size_t i = line;
	for (size_t y=2; y < manSeg.GetHeight(); ++y)
	{
		++i;

		for (size_t x=2; x < line; ++x, ++i)
		{
			if (af[i] > 0
			 && af[i-line-1] == af[i] && af[i] == af[i+line+1]
			 && ms[i] > 0 && z[i] == 0)
			{
				//found z-misaligned marker
				std::cout << currentFrameNo << " " << manSeg.GetX(i) << "," << manSeg.GetY(i) << "," << desiredZ << " seg label: " << ms[i] << "\n";
			}
		}

		++i;
	}

	return 0;
}

template <typename V>
void collisionReportingMakeAF(const i3d::Image3d<V>& fullImg, i3d::Image3d<V>& afImg)
{
	afImg.CopyMetaData(fullImg);
	afImg.MakeRoom(afImg.GetSizeX(),afImg.GetSizeY(),1);
	afImg.GetVoxelData() = 0;

	const V* fP = fullImg.GetFirstVoxelAddr();
	V* const aP = afImg.GetFirstVoxelAddr();

	for (size_t z = 0; z < fullImg.GetSizeZ(); ++z)
	{
		for (size_t i = 0; i < fullImg.GetSliceSize(); ++i)
		if (fP[i] > 0)
		{
			if (aP[i] > 0 && aP[i] != fP[i]) //collision
				std::cout << "COLLISION between labels " << aP[i] << " and " << fP[i]
				          << " at [" << afImg.GetX(i) << "," << afImg.GetY(i) << "]\n";
			else
				aP[i] = fP[i];
		}
		fP += fullImg.GetSliceSize();
	}
}
